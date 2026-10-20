# CCD-7882: controlled error responses

Branch `CCD-7882_Verbose_Error_Messages` was created from local `master` on 20 October 2026.

REST I/O, Elasticsearch, persistence, concurrency, bad-input and not-found errors use generic messages and category codes. Importer diagnostic errors and framework errors are sanitised. Explicit spreadsheet/field validation remains available. Upstream status mapping and required headers remain. Unhandled errors use the safe Boot fallback.

Spring Boot JSON fallback omits exception, message, trace, binding errors and reflected paths even when requested through query parameters. HTML fallback uses a fixed generic page. Health status remains available; component diagnostics are hidden.

Codes match the existing CPO, document-AM and translation categories: INVALID_REQUEST (400/422), AUTHENTICATION_REQUIRED (401), ACCESS_DENIED (403), RESOURCE_NOT_FOUND (404), METHOD_NOT_ALLOWED (405), REQUEST_TOO_LARGE (413), UNSUPPORTED_MEDIA_TYPE (415), TOO_MANY_REQUESTS (429), INTERNAL_ERROR (5xx), otherwise REQUEST_FAILED. Codes describe categories, not individual incidents. Boot fallback and empty security responses do not promise a category code.

## Local verification

793 tests across rest-api, excel-importer and the focused application fallback tests: zero failures/errors, two pre-existing skips; 791 passed. Main/test Checkstyle passed for the three changed modules. REST not-found fixtures and the health fixture were updated.

## Deployment handoff

These are local source/test results. Deployed BEFTA/ITHC, ingress/upstream error pages and actual dependency failures still require retesting. Do not infer restricted log access, Application Insights collection or production deployment from these checks. Existing internal diagnostics may contain sensitive information; use the existing restricted access controls. Authentication validation, endpoint permissions and successful responses were not deliberately changed.

## Review fixes

Import failure summaries are now generic when stored and when returned by ImportJobResponse,
so legacy persisted diagnostics cannot bypass the response handlers. Original import failures remain
available in DEBUG logs. Regression tests exercise both the DTO and the job-status HTTP response.

Reviewed missing-case-field, missing-layout-field and duplicate-category-display-order rules use
ImportValidationException with an enum of approved public messages. Technical MapperException messages
and causes remain private. Existing structured spreadsheet validation remains available. Other
unapproved exception messages remain generic; definition administrators must sign off the resulting
validation experience before release.

720 excel-importer tests passed, zero failures/errors/skips; main/test Checkstyle passed.
This includes the relevant parsers, upload service, handler, DTO and job-status controller.
