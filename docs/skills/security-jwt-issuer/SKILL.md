---
name: ccd-definition-store-security-jwt-issuer
description: Use when working in the HMCTS `ccd-definition-store-api` repository on JWT issuer validation, OIDC discovery versus enforced issuer configuration, Helm/Jenkins OIDC_ISSUER settings, build-integrated issuer verification, or related regression testing.
---

# Security JWT Issuer

## Overview

Use this skill for JWT issuer validation changes in `ccd-definition-store-api`.

## Workflow

1. Check current state with `git status --short` and inspect local diffs before editing.
2. Review [`application/src/main/java/uk/gov/hmcts/ccd/definition/store/SecurityConfiguration.java`](../../../application/src/main/java/uk/gov/hmcts/ccd/definition/store/SecurityConfiguration.java), [`application/src/main/resources/application.properties`](../../../application/src/main/resources/application.properties), [`charts/ccd-definition-store-api/values.yaml`](../../../charts/ccd-definition-store-api/values.yaml), [`Jenkinsfile_CNP`](../../../Jenkinsfile_CNP), and [`Jenkinsfile_nightly`](../../../Jenkinsfile_nightly).
3. Confirm the split between discovery and enforcement:
   `spring.security.oauth2.client.provider.oidc.issuer-uri` is for discovery/JWKS.
   `oidc.issuer` / `OIDC_ISSUER` is the enforced issuer matched against the token `iss` claim.
4. Search for `issuer`, `issuer-uri`, `JwtDecoder`, `JwtIssuerValidator`, `JwtTimestampValidator`, `OIDC_ISSUER`, and `VERIFY_OIDC_ISSUER` before changing behavior.
5. Preserve the single build-integrated verifier pattern:
   use [`aat/src/aat/java/uk/gov/hmcts/ccd/definitionstore/befta/JwtIssuerVerificationApp.java`](../../../aat/src/aat/java/uk/gov/hmcts/ccd/definitionstore/befta/JwtIssuerVerificationApp.java)
   and [`aat/build.gradle`](../../../aat/build.gradle) task `verifyFunctionalTestJwtIssuer`
   rather than duplicate smoke/functional verifier tests or Jenkins-side issuer-resolution scripts.
6. If authenticated setup runs before smoke or functional tests, keep issuer verification ahead of that setup path.
7. The verifier currently acquires its real token with `DEFINITION_IMPORTER_USERNAME` and `DEFINITION_IMPORTER_PASSWORD`. Keep any verifier credential changes aligned with the repo's actual setup/test credential path.
8. Do not guess `OIDC_ISSUER`. Decode a real token from the target environment and keep Jenkins-exported and app-enforced issuer values aligned.
9. Keep coverage focused:
   validator-level tests in [`application/src/test/java/uk/gov/hmcts/ccd/definition/store/SecurityConfigurationTest.java`](../../../application/src/test/java/uk/gov/hmcts/ccd/definition/store/SecurityConfigurationTest.java)
   and decoder integration coverage in [`application/src/test/java/uk/gov/hmcts/net/ccd/definition/store/security/JwtDecoderIssuerValidationIT.java`](../../../application/src/test/java/uk/gov/hmcts/net/ccd/definition/store/security/JwtDecoderIssuerValidationIT.java).

## References

- Primary repo guidance: [`docs/security/jwt-issuer-validation.md`](../../../docs/security/jwt-issuer-validation.md)
- Security workflow: [`docs/skills/security/SKILL.md`](../security/SKILL.md)
