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
| Existing rows | Same-version snapshot rows are kept. | Runtime replaces known metadata. |
| API response | Cached and uncached paths returned normal `CaseType`. | Same public response contract. |
| DB-to-HTTP path | `jsonb` to Jackson to Spring. | Same; not raw HTTP JSON streaming. |
| Performance test | Asserted fixed latency improvement. | Verifies cached and uncached responses are equivalent. |
| Index/query match | Index uses lower-case reference and version. | Lookup still uses exact reference and version. |

Snapshot rows are versioned by case type definition version, not by snapshot JSON shape.
If code changes the snapshot payload format without a new case type import,
old rows remain until a higher case type version is imported or rows are rebuilt.
This change protects metadata by replacing known metadata fields at response time.

## Verification

Run:

```bash
./gradlew :domain:test \
  --tests uk.gov.hmcts.ccd.definition.store.domain.service.casetype.CaseTypeSnapshotServiceTest \
  --tests uk.gov.hmcts.ccd.definition.store.domain.service.casetype.CaseTypeServiceImplTest
./gradlew :application:test --tests uk.gov.hmcts.net.ccd.definition.store.rest.CaseTypeSnapshotDisabledIT
./gradlew :application:compileTestJava
./gradlew :domain:checkstyleMain :domain:checkstyleTest :application:checkstyleTest
```

Recommended pipeline coverage:

- snapshot repository and JDBC integration tests
- case type snapshot endpoint integration tests
- broader `/case-type` BEFTA scenarios if time allows
- ensure local `.DS_Store` files are not committed
