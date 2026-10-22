# CCD-7882: controlled error responses

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

Reviewed missing-case-field, missing-layout-field and duplicate-category-display-order rules use
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

The original F-092 and F-099 functional fixtures are unchanged. Existing
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
