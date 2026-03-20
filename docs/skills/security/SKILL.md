---
name: ccd-definition-store-security
description: Use when working in the HMCTS `ccd-definition-store-api` repository on JWT issuer validation, Spring Security configuration, IDAM/OIDC integration, Helm/Jenkins issuer settings, or related regression testing. This skill is for resuming in-flight security patches, checking local diffs, validating discovery vs enforced issuer behavior, and using the single build-integrated OIDC issuer verifier pattern in this repo.
---

# Security

## Overview

Use this skill for security changes in `ccd-definition-store-api`, especially around JWT issuer validation and the CI issuer verifier.

## Workflow

1. Check current state with `git status --short` and inspect local diffs before editing.
2. Review [`application/src/main/java/uk/gov/hmcts/ccd/definition/store/SecurityConfiguration.java`](../../../application/src/main/java/uk/gov/hmcts/ccd/definition/store/SecurityConfiguration.java) together with [`application/src/main/resources/application.properties`](../../../application/src/main/resources/application.properties) and [`charts/ccd-definition-store-api/values.yaml`](../../../charts/ccd-definition-store-api/values.yaml).
3. Confirm the split between discovery and enforcement:
   `spring.security.oauth2.client.provider.oidc.issuer-uri` is for discovery/JWKS.
   `oidc.issuer` / `OIDC_ISSUER` is the enforced issuer matched against the token `iss` claim.
4. Search for `issuer`, `JwtDecoder`, `JwtIssuerValidator`, `JwtTimestampValidator`, `OIDC_ISSUER`, and `VERIFY_OIDC_ISSUER` before changing behavior.
5. Preserve the single build-integrated verifier pattern:
   use [`aat/src/aat/java/uk/gov/hmcts/ccd/definitionstore/befta/JwtIssuerVerificationApp.java`](../../../aat/src/aat/java/uk/gov/hmcts/ccd/definitionstore/befta/JwtIssuerVerificationApp.java)
   and [`aat/build.gradle`](../../../aat/build.gradle) task `verifyFunctionalTestJwtIssuer`
   rather than duplicate smoke/functional verifier tests or Jenkins-side issuer-resolution scripts.
6. Do not guess `OIDC_ISSUER`. For environment changes, derive it from a real token and keep Jenkins-exported `OIDC_ISSUER` aligned with deployment values.
7. Keep coverage focused:
   validator-level tests in [`application/src/test/java/uk/gov/hmcts/ccd/definition/store/SecurityConfigurationTest.java`](../../../application/src/test/java/uk/gov/hmcts/ccd/definition/store/SecurityConfigurationTest.java)
   and decoder integration coverage in [`application/src/test/java/uk/gov/hmcts/net/ccd/definition/store/security/JwtDecoderIssuerValidationIT.java`](../../../application/src/test/java/uk/gov/hmcts/net/ccd/definition/store/security/JwtDecoderIssuerValidationIT.java).

## References

- Primary repo guidance: [`docs/security/jwt-issuer-validation.md`](../../../docs/security/jwt-issuer-validation.md)
- CI wiring: [`Jenkinsfile_CNP`](../../../Jenkinsfile_CNP), [`Jenkinsfile_nightly`](../../../Jenkinsfile_nightly)
