# JWT issuer validation

## Service

`ccd-definition-store-api`

## Summary

- JWT issuer validation is enabled in the active `JwtDecoder`.
- OIDC discovery and issuer enforcement are configured separately on purpose.
- The enforced issuer must be taken from a real IDAM access token `iss` claim, not inferred from discovery metadata or deployment naming.

## Discovery vs enforced issuer

- `spring.security.oauth2.client.provider.oidc.issuer-uri` is the discovery location. The service uses it to load OIDC metadata and the JWKS endpoint.
- `oidc.issuer` / `OIDC_ISSUER` is the enforced issuer value. The active `JwtDecoder` validates the token `iss` claim against this value.
- These values can differ. Discovery can point at the public IDAM OIDC endpoint while enforcement pins the exact `iss` emitted in real access tokens.

## Runtime behavior

- `SecurityConfiguration.jwtDecoder()` builds the decoder from `issuer-uri`.
- The decoder then applies both `JwtTimestampValidator` and `JwtIssuerValidator(oidc.issuer)`.
- Tokens signed by the discovered JWKS are still rejected if their `iss` does not exactly match `OIDC_ISSUER`.

## Why this changed

- The previous decoder wiring validated timestamps only.
- The issuer validator had been commented out as part of an older migration workaround.
- That left the service accepting any correctly signed, unexpired token from the discovered JWKS, even if the token came from an unexpected issuer.
- The current configuration restores single-issuer enforcement.

## Coverage

- Unit coverage in `application/src/test/java/uk/gov/hmcts/ccd/definition/store/SecurityConfigurationTest.java` checks valid issuer, invalid issuer, and expired token behaviour at validator level.
- Decoder exception coverage in `application/src/test/java/uk/gov/hmcts/ccd/definition/store/security/JwtDecoderExceptionTest.java` checks decoder-level issuer and expiry failures without the broader Spring integration harness.
- Integration coverage in `application/src/test/java/uk/gov/hmcts/net/ccd/definition/store/security/JwtDecoderIssuerValidationIT.java` exercises the active decoder against WireMock-backed OIDC discovery and JWKS responses.

## Test and pipeline verification

- Focused application tests cover valid issuer, invalid issuer, and expired token cases.
- A single build-integrated verifier acquires a real IDAM access token and compares its `iss` claim to `OIDC_ISSUER` before authenticated setup, smoke, and functional runs.
- The verifier currently uses the importer test credentials, matching this repo's existing setup/test path.
- Local runs skip this live check unless `VERIFY_OIDC_ISSUER=true`.
- Jenkins sets `VERIFY_OIDC_ISSUER=true` and exports `OIDC_ISSUER` explicitly for the verifier.
- This is required because the verifier runs in the build container, where Helm-injected runtime environment variables are not directly visible.

## Operational guidance

- Do not invent `OIDC_ISSUER`.
- Resolve it from a real access token for the target environment and export that exact value into the test job.
- If Helm or deployment values already define `OIDC_ISSUER`, keep them aligned with the resolver output. A mismatch now fails the build-integrated verifier.
- The current enforced issuer value in this repo was verified from a real pipeline token and must stay aligned between Jenkins and deployment config.
- If temporary multi-issuer support is ever needed, add an explicit allow-list validator rather than disabling issuer validation again.
