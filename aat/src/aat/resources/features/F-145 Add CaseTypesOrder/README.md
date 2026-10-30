# F-145: CaseTypesOrder

These scenarios test CCD-7772 using the clarified requirement: `caseTypeOrder` selects
case types with that exact value, with matches sorted by case-type ID. With no
query parameter, no case-type sequence is required. This differs from the original
AC01 wording about ascending numeric sorting.

| Scenario | Evidence |
| --- | --- |
| S-145.1 | HTTP 200; filter 63 returns exactly eight matches in ascending ID order, each with numeric `caseTypesOrder: 63`. |
| S-145.2 | HTTP 200; legacy definition without the column returns all 10 case types, omits `caseTypesOrder`, and allows any sequence (AC02). |
| S-145.3 | HTTP 200; absent filter returns all 18 configured and unconfigured case types in any sequence. |
| S-145.4 | HTTP 200; filter 1 returns both case types with order 1, sorted by ID. |
| S-145.5 | HTTP 200; filter 99 returns both case types with order 99, sorted by ID. |
| S-145.6 | HTTP 200; no matching order returns the jurisdiction with an empty case-type list. |

The mixed fixture contains 18 case types: eight with order 63, three with order 7,
two each with orders 1 and 99, and three with blank order values. The legacy fixture
contains 10 case types and has no CaseTypesOrder heading. Every case type includes
a state, event, field and the required layouts.

Import order, ID order and display-name order deliberately differ. S-145.1 checks
the exact eight-ID sequence, excluding the other 10 case types. S-145.2 and S-145.3
use EQUIVALENT/UNORDERED comparisons by ID, checking complete membership, numeric
values and omission for blank values without requiring random or unsorted output.
Their expected lists deliberately differ from fixture import order.

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
