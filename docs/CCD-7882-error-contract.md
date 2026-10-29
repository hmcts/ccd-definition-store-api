# CCD-7882: controlled error responses

## Validation compatibility review — 29 October 2026

The review found that generic MapperException handling also removed useful spreadsheet
validation feedback. Fixed public templates now preserve duplicate DefaultFocus,
NullifyByDefault combined with DefaultValue, Publish on a COMPLEX field, required
worksheet/case-type/jurisdiction rules, missing tab fields and duplicate layout rows.
Arbitrary MapperException messages and nested technical causes remain private.
The workbook contract runner now includes F-112 and F-144 and checks text/plain for
plain-text validation bodies. Existing functional fixture expectations were retained.

Verification: all 99 workbook contract cases passed, including S-112.3, S-144.2
and S-144.5. All 69 focused parser/validator/handler tests and the inherited layout
parser tests passed. Importer main/test and application test Checkstyle passed.

The follow-up review also preserves blank required-cell feedback, unknown layout
access profiles, invalid ordering patterns, duplicate/missing sort priorities,
missing workbook definition names and excessive column lengths. The original
validation exception families and message text are retained. Real parser/validator
failures are now checked through Spring MVC for HTTP 400, text/plain and the exact
validation message; suppressed technical diagnostics must not appear in that body.
All 118 focused tests passed. Importer main/test Checkstyle passed. The earlier
99-workbook run was not repeated for this follow-up.

## Current support logging policy

Public error responses remain generic. Detailed request failures, including exception causes
and stack traces, are retained at normal WARN/ERROR levels in server logs; DEBUG is optional
additional troubleshooting. Dependency and health diagnostics retain their originating severity
and respect the source logger threshold. Support must not need to enable DEBUG and reproduce
an incident to obtain its original failure details.

This policy supersedes the DEBUG-only descriptions and test expectations in earlier verification
entries below. Those entries describe historical checks. Deployment acceptance still requires
administrator-only log access, suitable retention and verification of logging/Application Insights
collection. Local tests do not establish those infrastructure controls.


Branch `CCD-7882_Verbose_Error_Messages` was created from local `master` on 20 October 2026.

REST I/O, Elasticsearch, persistence, concurrency, bad-input and not-found errors use generic messages and category codes. Importer diagnostic errors and framework errors are sanitised. Explicit spreadsheet/field validation remains available. Upstream status mapping and required headers remain. Unhandled errors use the safe Boot fallback.

Spring Boot JSON fallback retains a generic HTTP reason in message and a fixed path (/import for import failures, / otherwise). Exception details, trace, binding errors and arbitrary reflected paths are omitted even when requested through query parameters. HTML fallback uses a fixed generic page. Health status remains available; component diagnostics are hidden.

Error categories: INVALID_REQUEST (400/422), AUTHENTICATION_REQUIRED (401), ACCESS_DENIED (403), RESOURCE_NOT_FOUND (404), METHOD_NOT_ALLOWED (405), REQUEST_TOO_LARGE (413), UNSUPPORTED_MEDIA_TYPE (415), TOO_MANY_REQUESTS (429), INTERNAL_ERROR (5xx), otherwise REQUEST_FAILED. Codes describe categories, not individual incidents. Boot fallback and empty security responses do not promise a category code.

## Local verification

793 tests across rest-api, excel-importer and the focused application fallback tests: zero failures/errors, two pre-existing skips; 791 passed. Main/test Checkstyle passed for the three changed modules. REST not-found fixtures and the health fixture were updated.

## Deployment handoff

These are local source/test results. Deployed BEFTA/ITHC, ingress/upstream error pages and actual dependency failures still require retesting. Do not infer restricted log access, Application Insights collection or production deployment from these checks. Existing internal diagnostics may contain sensitive information; use the existing restricted access controls. Authentication validation, endpoint permissions and successful responses were not deliberately changed.

## Review fixes

Import failure summaries are now generic when stored and when returned by ImportJobResponse,
so legacy persisted diagnostics cannot bypass the response handlers. Original import failures remain
available in server logs at the HTTP boundary without requiring DEBUG. Regression tests exercise both the DTO and the job-status HTTP response.

Reviewed missing-case-field and missing-layout-field rules use
ImportValidationException with an enum of approved public messages. Technical MapperException messages
and causes remain private. Existing structured spreadsheet validation remains available. Other
unapproved exception messages remain generic; definition administrators must sign off the resulting
validation experience before release.

720 excel-importer tests passed, zero failures/errors/skips; main/test Checkstyle passed.
This includes the relevant parsers, upload service, handler, DTO and job-status controller.

## Authoritative remediation and support requirement

The supplied CCD-7882 finding concerns unauthenticated disclosure of implementation details
through HTTP error responses. This note covers the remediation and verification for this service.

The remediation requires generic public error pages/responses and states: "The server should
still log the detailed information, but access to the log will be restricted to administrators."
An event reference is optional. It does not prescribe DEBUG-only logging or require support
to restart a service to obtain diagnostics.

Response sanitisation and administrator diagnostics are separate requirements. DEBUG-only
implementation details described elsewhere in this note record the implementation, not an
acceptance criterion from the finding. If production DEBUG is disabled and no other approved
mechanism records the detailed failure, that is a support/acceptance gap: enabling DEBUG after
an incident cannot recover its missing diagnostics. A safe category/status summary alone does
not satisfy the detailed-log requirement.

Before release, verify that detailed failures are recorded and accessible only to administrators,
including the deployed logging/Application Insights configuration, while public responses stay
generic. Preserve useful validation responses and existing protocol behaviour. Select and test
the logging policy against these requirements; do not assume that moving every diagnostic to
ERROR, or enabling DEBUG globally, is the required solution. Existing local test results do not
establish this deployed acceptance. This requirement also applies to the current support logging policy above.

## Support logging verification — 21 October 2026

48 focused tests passed, zero failures/errors/skips. Checks cover the applicable request/error
paths and confirm that public responses remain sanitised while original exception diagnostics
are retained without enabling DEBUG. Java main/test Checkstyle passed.
The focused application tests used Docker PostgreSQL through Testcontainers.
Administrator-only log access, retention and deployed collection remain environment acceptance checks.

## Docker verification with DEBUG off and on — 21 October 2026

Both runs used PostgreSQL 15 in Docker through Testcontainers and executed all nine
SpreadSheetImportTest tests: zero failures/errors/skips in each run. INFO produced no DEBUG
lines. DEBUG produced additional tracing, with one original exception event in both modes.
The upstream 403 scenario asserts exactly one original exception event at ERROR across the
root logger and the same safe HTTP 500 response in each mode:

```json
{"message":"Internal Server Error","code":"INTERNAL_ERROR"}
```

Server diagnostics retain HttpClientErrorException$Forbidden, its original 403 message and
stack trace. Expected client errors use WARN in the modified definition handlers. The HTTP
advice owns propagated import diagnostics; intermediate layers do not log the same exception.
Set CCD_TEST_LOG_LEVEL=INFO or DEBUG to reproduce the two local modes. This variable controls
test logging only. Administrator-only access and deployed collection remain environment checks.

## Validation compatibility — 22 October 2026

The F-092 and F-099 import validation fixtures used by the regression test are unchanged. Existing
InvalidImportException and SpreadsheetParsingException types now expose approved
validation feedback through fixed-message factories. Arbitrary exception messages
and technical causes remain private. No additional exception types were introduced.
The HTTP boundary logs the exception and its cause once; constructors do not duplicate it.

Post-state validation retains timestamp, status, error, message and the fixed /import
path. Field-reference and collection validation retain their original text responses.
Only approved templates and values supplied in the workbook are returned publicly.

All 33 negative F-092/F-099 cases passed locally against their existing fixtures and
real workbooks using Docker-backed integration tests. Nine existing spreadsheet
integration tests, four fallback tests and 165 focused importer tests also passed
(211 total). Main/test Checkstyle passed for application and excel-importer.
The integration test seeds the access profiles required by the BEFTA workbooks;
production permissions are unchanged. Deployed BEFTA/ITHC and administrator-only
log access/collection still require environment verification.

Missing-case-field and missing-layout-field failures retain their original case type
and layout context in the private exception message for WARN logging. The existing
ImportValidationException carries this diagnostic separately from its approved public
reason; HTTP responses remain unchanged. No new exception class or logger was added.

The functional import contract tests clean up committed import_jobs rows after each
case using Spring's isolated SQL test cleanup. Job transactions use REQUIRES_NEW,
so rolling back the workbook test transaction alone does not remove them. Without
this cleanup, the 33 cases left rows that broke the import-job integration test's
single-row assertion when both classes ran together. Production code is unaffected.
After the fix, the full Docker-backed application suite passed: 89 passed, five
skipped, zero failures/errors. Application test Checkstyle passed.


## Additional validation compatibility — 22 October 2026

Approved retainHiddenValue messages use fixed factories on the existing
ImportValidationException, including safe aggregation of complex-field errors.
Duplicate category display order uses the original InvalidImportException path so
CategoryParser retains HTTP 422 and the original validation text. The redundant
DUPLICATE_DISPLAY_ORDER reason was removed from ImportValidationException.

Invalid spreadsheet values and missing columns use approved templates rather than
arbitrary MapperException messages. This covers Publish, ReadOnly, Disabled,
AccessProfile, TTLIncrement, GroupAccessEnabled and AccessType boolean columns.
CategoryID checks use fixed-message factories on the existing InvalidImportException.
Technical errors remain generic; mixed validation/technical batches remain generic
and retain original failures for support logging.

The existing workbook regression test seeds the BEFTA access profiles and enables
the existing group-access feature in its own test context. Production feature flags
and permissions are unchanged. It also reads the unchanged negative fixtures
for F-093, F-095 Publish, F-097, F-098, F-104, F-105, F-106, F-109, F-111 and F-127.
F-127's preview HTTP 500 must be investigated separately using the original server
exception. Its parser and validator are unchanged against master. S-127.1 uses a
pipeline-generated local workbook and is not covered by this negative-fixture runner.


Latest verification used Docker PostgreSQL via Testcontainers with the application
running in the Spring integration-test JVM. At INFO, the full application suite
passed: 152 passed, five skipped, zero failures/errors, including all 96 negative
workbook fixtures. The 86 focused importer tests passed; main/test importer
Checkstyle and application test Checkstyle passed.

The DEBUG repeat passed all 13 spreadsheet/fallback tests. The captured spreadsheet
suite contained zero DEBUG lines at INFO and 1873 at DEBUG. The upstream-403
regression asserts one original ERROR event, its original message and stack frames,
and identical sanitised HTTP 500 JSON in both modes. No DEBUG-only setting is needed
to retain the original exception. This verifies local developer tracing, not deployed
log access/retention or dynamic production logger reconfiguration.

Example developer DEBUG event from the local run:

```text
DEBUG CaseFieldParser: Parsing case fields for case type TestAddressBookCase...
```

The public response in both runs remained:

```json
{"message":"Internal Server Error","code":"INTERNAL_ERROR"}
```

## Paired application-container verification — 22 October 2026

Malformed bearer challenges no longer disclose decoder descriptions or URIs. Existing authentication statuses and issuer/timestamp validation were preserved. SafeAuthenticationErrors tests and application main/test Checkstyle passed; the application explicitly depends on commons for the shared code enum.

Both INFO (DEBUG off) and DEBUG runs passed all six checks: anonymous JSON/HTML,
invalid user bearer or invalid service credentials as appropriate, authenticated dependency failure,
health-detail redaction and retained original server diagnostics. This is 12 checks for this service.
Support logging is the policy; authentication logging is one covered failure category.
Neither requires enabling DEBUG to retain original failures.

The local verification driver is review-reports/CCD-7882/verify_docker.py in the parent workspace.
Run from the workspace root with --repo ccd-definition-store-api (both modes are the default).
The definition-store Java bootJar was used with synthetic OIDC/S2S/services and PostgreSQL.
Database migrations were used for startup. Java used the cached HMCTS Java 21 runtime;
this is a substitute runtime, not a build of the production Dockerfile.
Artifact hashes and per-check results are in review-reports/CCD-7882/docker-results.json.
Scripts allow repeat runs and results identify what was checked; retaining them is evidence,
not an extra production runtime dependency. Synthetic raw logs are local diagnostics.

Production image/ingress verification, deployed BEFTA/ITHC, actual Azure ingestion, retention
and administrator-only access remain release gates. The local telemetry collector proves the
specific agent/SDK path tested, not production collection settings.
