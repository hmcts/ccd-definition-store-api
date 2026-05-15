# JWT issuer validation

## Service

`ccd-definition-store-api`

## Summary

- JWT issuer validation is enabled in the active `JwtDecoder`.
- OIDC discovery and issuer enforcement are configured separately on purpose.
- The enforced issuer must be taken from a real IDAM access token `iss` claim, not inferred from discovery metadata or deployment naming.
- This service is compliant with the HMCTS guidance purpose: it validates JWT signature, expiry, and `iss` together.
- This service follows the HMCTS guidance pattern for API services that validate against a primary configured issuer with an optional exact issuer allow-list.
- Main runtime still keeps a compatibility fallback for `OIDC_ISSUER`, so the stricter no-fallback startup pattern is not applied here.
- `OIDC_ALLOWED_ISSUERS` must stay unset unless this service receives both public IDAM and ForgeRock-issued tokens.
- See [HMCTS Guidance](#hmcts-guidance) for the central policy reference.

## HMCTS Guidance

- [JWT iss Claim Validation guidance](https://tools.hmcts.net/confluence/spaces/SISM/pages/1958056812/JWT+iss+Claim+Validation+for+OIDC+and+OAuth+2+Tokens#JWTissClaimValidationforOIDCandOAuth2Tokens-Configurationrecommendation)
- Use that guidance as the reference point for service-level issuer decisions and configuration recommendations.

## Quick Reference

| Topic | Current repo position |
| --- | --- |
| Validation model | Primary configured issuer with optional exact allow-list |
| Discovery source | `spring.security.oauth2.client.provider.oidc.issuer-uri` |
| Primary enforced issuer | `oidc.issuer` / `OIDC_ISSUER` |
| Optional additional issuers | `oidc.allowed-issuers` / `OIDC_ALLOWED_ISSUERS` |
| Runtime rule | Token `iss` must exactly match `OIDC_ISSUER` or, when configured, one of the additional allowed issuers |

## Discovery vs enforced issuer

- `spring.security.oauth2.client.provider.oidc.issuer-uri` is the discovery location. The service uses it to load OIDC metadata and the JWKS endpoint.
- `oidc.issuer` / `OIDC_ISSUER` is the primary enforced issuer value. The active `JwtDecoder` always validates the token `iss` claim against this value.
- `oidc.allowed-issuers` / `OIDC_ALLOWED_ISSUERS` is an optional comma-separated list of additional exact issuer values. When unset or blank, validation falls back to `OIDC_ISSUER` only.
- `OIDC_ALLOWED_ISSUERS` is not populated automatically by Helm or Jenkins. It only takes effect when explicitly injected into the app's runtime environment, and Jenkins must also set it if the build-integrated verifier needs to allow the same additional issuer.
- These values can differ. Discovery can point at the public IDAM OIDC endpoint while enforcement pins the exact `iss` emitted in real access tokens.
- Do not infer the enforced issuer from discovery. Keep discovery and enforcement separate.

## Runtime behavior

- `SecurityConfiguration.jwtDecoder()` builds the decoder from `issuer-uri`.
- The decoder then applies both `JwtTimestampValidator` and an exact `iss` claim validator backed by `OIDC_ISSUER` plus optional `OIDC_ALLOWED_ISSUERS`.
- Tokens signed by the discovered JWKS are still rejected if their `iss` does not exactly match the configured issuer set.

## Why this changed

- The previous decoder wiring validated timestamps only.
- The issuer validator had been commented out as part of an older migration workaround.
- That left the service accepting any correctly signed, unexpired token from the discovered JWKS, even if the token came from an unexpected issuer.
- The current configuration restores issuer enforcement with a primary issuer and an optional explicit allow-list.

## Coverage

| Test area | Coverage |
| --- | --- |
| `SecurityConfigurationTest` | Primary issuer, additional allowed issuer, invalid issuer, missing `iss`, and expired token behaviour at validator level |
| `JwtDecoderExceptionTest` | Decoder-level primary issuer fallback, additional issuer, unexpected issuer, missing `iss`, and expiry behaviour without the broader Spring integration harness |
| `JwtDecoderIssuerValidationIT` | Active decoder against WireMock-backed OIDC discovery and JWKS responses, including optional additional issuers and missing `iss` rejection |

## Test and pipeline verification

- Focused application tests cover primary issuer fallback, additional allowed issuers, unexpected issuers, missing `iss`, and expired token cases.
- A single build-integrated verifier acquires a real IDAM access token and compares its `iss` claim to `OIDC_ISSUER` plus optional `OIDC_ALLOWED_ISSUERS` before authenticated setup, smoke, and functional runs.
- The verifier currently uses the importer test credentials, matching this repo's existing setup/test path.
- Local runs skip this live check unless `VERIFY_OIDC_ISSUER=true`.
- Jenkins sets `VERIFY_OIDC_ISSUER=true` and exports `OIDC_ISSUER` explicitly for the verifier.
- Jenkins does not see Helm-injected app runtime variables. If an environment enables `OIDC_ALLOWED_ISSUERS`, set the same value in Jenkins for smoke/functional verification.
- This is required because the verifier runs in the build container, where Helm-injected runtime environment variables are not directly visible.

## Operational guidance

- Do not invent `OIDC_ISSUER`.
- Do not invent `OIDC_ALLOWED_ISSUERS`; only configure it when the service receives both public IDAM and ForgeRock-issued tokens.
- Resolve issuer values from real access tokens for the target environment and export those exact values into the test job.
- If Helm or deployment values already define `OIDC_ISSUER`, keep them aligned with the resolver output. A mismatch now fails the build-integrated verifier.
- If `OIDC_ALLOWED_ISSUERS` is configured, keep it aligned with the same real-token evidence and include `OIDC_ISSUER` as the primary issuer. Configure it explicitly in Helm/deployment values for app runtime and in Jenkins for verifier runtime.
- The current enforced issuer value in this repo was verified from a real pipeline token and must stay aligned between Jenkins and deployment config.
- Production-like environments should provide `OIDC_ISSUER` explicitly through deployment configuration.
- Main runtime currently retains a compatibility fallback in `application.properties`.
- Because of that fallback, missing `OIDC_ISSUER` does not fail fast at startup. Missing `OIDC_ALLOWED_ISSUERS` falls back to `OIDC_ISSUER` only.
- Use [HMCTS Guidance](#hmcts-guidance) as the central policy reference for service-level issuer decisions.

## How to derive `OIDC_ISSUER`

- Do not guess the issuer from the public discovery URL alone.
- Decode only the JWT payload from a real access token for the target environment and inspect the `iss` claim.
- Do not store or document full bearer tokens. Record only the derived issuer value.

Example:

```bash
TOKEN='eyJ...'
PAYLOAD=$(printf '%s' "$TOKEN" | cut -d '.' -f2)
python3 - <<'PY' "$PAYLOAD"
import base64, json, sys
payload = sys.argv[1]
payload += '=' * (-len(payload) % 4)
print(json.loads(base64.urlsafe_b64decode(payload))["iss"])
PY
```

- JWTs are `header.payload.signature`.
- The second segment is base64url-encoded JSON.
- This decodes the payload only. It does not verify the signature.

## Acceptance Checklist

Before merging JWT issuer-validation changes, confirm all of the following:

- The active `JwtDecoder` is built from `spring.security.oauth2.client.provider.oidc.issuer-uri`.
- The active validator chain includes both `JwtTimestampValidator` and exact `iss` claim validation.
- There is no disabled, commented-out, or alternate runtime path that leaves issuer validation off.
- `issuer-uri` is used for discovery and JWKS lookup only.
- `oidc.issuer` / `OIDC_ISSUER` is used as the primary enforced token `iss` value only.
- `oidc.allowed-issuers` / `OIDC_ALLOWED_ISSUERS`, if configured, contains only additional exact token `iss` values verified from real tokens.
- For production-like environments, `OIDC_ISSUER` is explicitly configured and not guessed from the discovery URL.
- Repo config, Helm defaults, and CI/Jenkins defaults are intentionally aligned where this repo defines them; any environment-specific override is documented.
- If `OIDC_ISSUER` or `OIDC_ALLOWED_ISSUERS` changed, it was verified against real tokens for the target environment and that verification was recorded.
- There is a test that accepts a token with the expected issuer.
- There is a test that accepts a token with an additional allowed issuer.
- There is a test that rejects a token with an unexpected issuer.
- There is a test that rejects a token with a missing `iss` claim.
- There is a test that rejects an expired token.
- There is decoder-level coverage using a signed token, not only validator-only coverage.
- At least one failure assertion clearly proves issuer rejection, for example by checking for `iss`.
- CI or build verification includes a verifier path that can check a real token issuer is allowed by `OIDC_ISSUER` plus optional `OIDC_ALLOWED_ISSUERS`, or the repo documents why that does not apply.
- Comments and docs accurately describe the current secure behavior and clearly label any historical insecure behavior as historical.
- Any repo-specific difference from peer services is intentional and documented.

Do not merge if any of the following are true:

- issuer validation is constructed but not applied
- only timestamp validation is active
- `OIDC_ISSUER` or `OIDC_ALLOWED_ISSUERS` was inferred rather than verified
- `OIDC_ALLOWED_ISSUERS` is configured without evidence that this service receives both public IDAM and ForgeRock-issued tokens
- Helm and CI/Jenkins issuer values disagree without explanation
- only happy-path tests exist

## Configuration Policy

- `spring.security.oauth2.client.provider.oidc.issuer-uri` is used for OIDC discovery and JWKS lookup only.
- `oidc.issuer` / `OIDC_ISSUER` is the primary enforced JWT issuer and must match the token `iss` claim exactly.
- `oidc.allowed-issuers` / `OIDC_ALLOWED_ISSUERS` is optional and must only contain additional exact JWT issuer values.
- Do not derive `OIDC_ISSUER` or `OIDC_ALLOWED_ISSUERS` from `IDAM_USER_URL`, `idam.api.url`, or the discovery URL.
- Production-like environments should provide `OIDC_ISSUER` explicitly.
- Leave `OIDC_ALLOWED_ISSUERS` unset unless this service receives both public IDAM and ForgeRock-issued tokens.
- Main runtime currently uses a compatibility fallback and therefore does not require `OIDC_ISSUER` to be present at startup.
- This repo keeps discovery and enforced issuer separate.
- This repo does not infer the enforced issuer from discovery metadata.
- This repo is compliant with the HMCTS guidance purpose because issuer validation is active in the runtime validator chain.
- Local or test-only fallbacks are acceptable only when they are static, intentional, and clearly scoped to non-production use.
- The build enforces this policy with `verifyOidcIssuerPolicy`, which fails if issuer settings are derived from discovery config.

## References

- [HMCTS Guidance](#hmcts-guidance)
