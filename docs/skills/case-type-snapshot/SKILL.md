# Case Type Snapshot Notes

## Requirement

Precompute case type responses in PostgreSQL to reduce repeated `/case-type`
assembly cost and improve API latency for:

- `/api/data/case-type/{id}`
- `/api/data/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}`

The intended architecture is hybrid:

- eager creation after successful definition import
- lazy creation on first request if the eager snapshot is missing or failed
- one `case_type_snapshot` row per `case_type_reference`
- latest imported version replaces the previous snapshot row
- request failure must not be caused by snapshot persistence failure

## Configuration Requirements

| Property | Default | Behaviour |
| --- | --- | --- |
| `case-type.snapshot.enabled` | `true` | Global switch. `false` disables reads, writes, and eager creation. |
| `case-type.snapshot.async-enabled` | `true` | Eager-only switch. `false` disables post-import creation. |

When `case-type.snapshot.enabled=false`, lazy creation is also disabled.

## Current Implementation

Storage:

- `case_type_snapshot.case_type_reference`
- `case_type_snapshot.version_id`
- `case_type_snapshot.precomputed_response jsonb`
- `created_at`
- `last_modified`

Eager path:

- `ImportServiceImpl` publishes `SnapshotCreationEvent`.
- `AsynchronousSnapshotCreationListener` handles the event asynchronously.
- `SnapshotCreator` calls `CaseTypeService.findByCaseTypeId`.
- Missing snapshots are assembled and upserted.

Lazy path:

- `CaseTypeServiceImpl.findByCaseTypeId` checks `CaseTypeSnapshotService`.
- On cache miss, it loads the case type from repositories, maps it, stores a snapshot, and returns the response.
- Snapshot store failures are logged and swallowed so the API response can still succeed.

Repair path:

- An empty read combined with an existing row for that version means the stored payload could not be
  deserialized, so the row is broken.
- `CaseTypeSnapshotService` deletes it, which restores the ordinary cache-miss path and lets the store
  that follows the lookup rebuild it from the database.
- Without this the cache could not recover: `storeSnapshot` skips the write when a row already exists
  for the version, so every later request would fail the read and rebuild, indefinitely.
- The delete is scoped by reference *and* version, so a row written for a newer version by a
  concurrent import is never removed. Delete failures are swallowed like store failures.

Eager snapshot creation:

```text
                         EAGER SNAPSHOT CREATION

+----------------------+
| Case Definition      |
| Import Process       |
+----------+-----------+
           |
           | publishes SnapshotCreationEvent
           v
+----------------------------------------+
| Snapshot Listener (async)              |
| onSnapshotCreationRequested            |
+----------+-----------------------------+
           |
           | calls SnapshotCreator
           v
+----------------------------------------+
| CaseTypeService.findByCaseTypeId       |
+----------+-----------------------------+
           |
           | loads latest data if snapshot is missing
           v
+----------------------------------------+
| Repository (Case Type Data)            |
+----------+-----------------------------+
           |
           | serialize payload as jsonb
           v
+----------------------------------------+
| PostgreSQL case_type_snapshot          |
| UPSERT via ON CONFLICT                 |
+----------------------------------------+
```

Lazy snapshot creation:

```text
                         LAZY SNAPSHOT CREATION

+----------------------------------------+
| /case-type API client request          |
+----------+-----------------------------+
           |
           | try fetch snapshot
           v
+----------------------------------------+
| PostgreSQL case_type_snapshot          |
+----------+-----------------------------+
   found   |           | not found
           |           v
           |   +---------------------------+
           |   | Build payload on demand   |
           |   | from repositories         |
           |   +-----------+---------------+
           |               |
           |               | synchronous upsert
           |               v
           |   +---------------------------+
           |   | case_type_snapshot upsert |
           |   +---------------------------+
           |
           v
+----------------------------------------+
| Return normal CaseType response        |
| to client                              |
+----------------------------------------+
```

Read path:

- `SnapshotJdbcRepository` reads `jsonb` using `ResultSet.getBinaryStream()`.
- Jackson deserializes the stream into `CaseType`.
- The controller returns the normal `CaseType` response.

Important: this is not raw HTTP streaming from PostgreSQL to the client.
It streams from PostgreSQL into Jackson, then Spring serializes the `CaseType` response.

## Original Flow

Before the snapshot feature, every request rebuilt the response from repositories:

```text
                         ORIGINAL /case-type FLOW

+----------------------------------------+
| /case-type API client request          |
+----------+-----------------------------+
           |
           | find latest case type version
           v
+----------------------------------------+
| CaseTypeRepository                     |
+----------+-----------------------------+
           |
           | load full case type graph
           v
+----------------------------------------+
| Repository-backed entity graph         |
| events, fields, ACLs, states, etc.     |
+----------+-----------------------------+
           |
           | map entities to CaseType
           v
+----------------------------------------+
| EntityToResponseDTOMapper              |
+----------+-----------------------------+
           |
           | add runtime metadata fields
           v
+----------------------------------------+
| Return normal CaseType response        |
+----------------------------------------+
```

## Implementation Notes

| Area | Before | Current |
| --- | --- | --- |
| Feature flags | Old flag stopped eager only; lazy still ran. | Global stops all; async stops eager. |
| Snapshot content | Stored `CaseType` after metadata was added. | Stores payload before metadata decoration. |
| Existing rows | Same-version snapshot rows are kept. | Runtime replaces static metadata. |
| API response | Cached and uncached paths returned normal `CaseType`. | Same public response contract. |
| DB-to-HTTP path | `jsonb` to Jackson to Spring. | Same; not raw HTTP JSON streaming. |
| Performance test | Asserted fixed latency improvement. | Verifies cached and uncached responses are equivalent. |
| Unreadable rows | Read failed, row kept, cache stuck until re-import. | Row is discarded and rebuilt on the same request. |
| Indexing | Added a `LOWER(reference), version_id` index. | Dropped: no query uses `LOWER`, and `UNIQUE (case_type_reference)` already indexes the lookup. |

## Response shape drift

Snapshot rows are keyed by case type definition **version**, not by the shape of the stored JSON.
Changing the response model is a **code** change, so the version does not move and the key still
matches. Rows written before the change keep being served in the old shape until each case type is
imported again.

Which way that matters depends on the direction of the change:

- **Field removed** - deserialization fails, the repair path above discards the row and rebuilds it.
  Self-healing, nothing to do.
- **Field added, populated by new import data** - harmless. A case type that has not been re-imported
  has no value for it either way, so cached and rebuilt responses agree.
- **Field added, exposing data already in the database** - the snapshot returns null while a rebuild
  returns the real value, and nothing reports the disagreement. `case_type.live_from` / `live_to` are
  a live example: populated on every import, not currently in the response.

`CaseTypeResponseShapeGuardTest` fails the build on any change to the serialized shape - field added
or removed, `@JsonIgnore` added or removed, `@JsonProperty` renamed - and asks the author to decide
which case applies. When it is the third, invalidate the cache in the same release with a migration
containing `DELETE FROM case_type_snapshot;`.

Note that such a migration runs at pod startup, so during a rolling deploy the cache is briefly cold
fleet-wide. Prefer a low-traffic window for a large jurisdiction set.

Static metadata is protected by being replaced at response time. Dynamic metadata such as `[STATE]`
remains in the case-type payload.

## Verification

Run:

```bash
./gradlew :domain:test --tests '*CaseTypeSnapshotServiceTest' --tests '*CaseTypeServiceImplTest'
./gradlew :repository:test --tests '*CaseTypeSnapshotRepositoryTest' --tests '*SnapshotJdbcRepositoryTest' \
  --tests '*CaseTypeResponseShapeGuardTest'
./gradlew :application:test --tests '*CaseTypeSnapshot*' --tests '*SpreadSheetImportTest'
./gradlew :repository:checkstyleMain :repository:checkstyleTest \
  :domain:checkstyleMain :domain:checkstyleTest :application:checkstyleTest
```

Recommended pipeline coverage:

- snapshot repository and JDBC integration tests
- case type snapshot endpoint integration tests
- the response shape guard test, which gates cache invalidation on model changes
- broader `/case-type` BEFTA scenarios - the cached and rebuilt paths must agree byte for byte, and
  only one integration test covers that, on a single case type
