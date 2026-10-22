# F-145: CaseTypesOrder

These scenarios test CCD-7772 using the clarified requirement: `caseTypeOrder` selects
case types with that exact value, with matches sorted by case-type ID. With no
query parameter, no case-type sequence is required. This differs from the original
AC01 wording about ascending numeric sorting.

| Scenario | Evidence |
| --- | --- |
| S-145.1 | HTTP 200; filter 42 returns only F145_AMatch (Zulu) then F145_ZMatch (Alpha), both with numeric `caseTypesOrder: 42`. |
| S-145.2 | HTTP 200; legacy definition without the column returns both case types, omits `caseTypesOrder`, and allows any sequence (AC02). |
| S-145.3 | HTTP 200; absent filter returns all configured and unconfigured case types in any sequence. |
| S-145.4 | HTTP 200; filter 1 returns the case type with order 1. |
| S-145.5 | HTTP 200; filter 99 returns the case type with order 99. |
| S-145.6 | HTTP 200; no matching order returns the jurisdiction with an empty case-type list. |

The two matching case-type IDs have the opposite alphabetical order to their names,
so sorting by name would fail S-145.1.

Each scenario imports its dedicated definition as a prerequisite. The existing BEFTA
loader generates `BEFTA_F145_ORDERED.xlsx` and `BEFTA_F145_LEGACY.xlsx` from the JSON
fixtures under `uk/gov/hmcts/ccd/test_definitions/valid`. Blank template rows provide
headings for required empty sheets. The fixtures do not change user profiles or ACLs.

Run against the configured Definition Store environment with the usual BEFTA
credentials and service dependencies available:

```sh
./gradlew :aat:functional -Ptags=@F-145
```

The existing Gradle task generates the functional Cucumber report. A Cucumber dry
run checks step bindings only; it does not provide evidence of HTTP execution.
