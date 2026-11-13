# CCD-7882 error contract

Related requirements: CCD-7108 and CME-984.

Prevent public errors from revealing exception classes, stack traces, internal URLs, file paths
or software details. Preserve response schemas, HTTP status mappings, approved business messages
and public `message` fields where present, while retaining useful private diagnostics.

## Responses

- Disable automatic exception messages, binding errors and stack traces. Hide health details and
  native Tomcat reports/version information; return controlled fallback content.
- Replace technical REST/import failure details with generic text while retaining each handler's
  response format, status mapping and required headers. Framework fallback paths use a safe message
  and `path: "/"`.
- Keep approved spreadsheet, field and business validation messages. Unapproved parser/mapper
  details are sanitised.
- Failed import-job responses expose `Unable to complete the import.` instead of stored diagnostics.
  Import processing stores a controlled failure summary and logs the original exception.
- Sanitise bearer challenges without decoder descriptions or internal URLs. Add no public
  category-code field.

## Diagnostics

`ErrorCode` labels are internal log categories; `failureCode` is the structured telemetry code.
Neither introduces a public API error-code field.

Error-handler logging retains original exceptions with private `failureCode` and `httpStatus`
dimensions and preserved correlation context.

Bearer authentication and access-denied handler logs carry private `failureCode` and `httpStatus`
dimensions; the diagnostic scope restores the caller's context after logging.

## Verification

Focused REST, importer, fixture-contract, authentication and diagnostic tests cover these changes.
Docker checks cover malformed requests, correlated failure telemetry and DEBUG ingestion with
application/agent DEBUG enabled.

## Support

In Application Insights, filter private `failureCode` and `httpStatus` dimensions, then use
`operation_Id` to find related requests and dependencies. Inspect exception details in restricted
telemetry or server logs; public error messages are sanitised. Failure diagnosis should work at
normal production logging levels without enabling DEBUG.

## Deployment checks

- Verify effective error/logging settings and sanitised responses in each environment. Confirm
  `server.error.include-message=never` at runtime.
- Confirm diagnostic logs and Application Insights are restricted to authorised personnel, with
  suitable sampling and retention.
- Verify failure codes and correlation IDs are searchable at normal production logging levels;
  run deployed consumer/Pact checks.
