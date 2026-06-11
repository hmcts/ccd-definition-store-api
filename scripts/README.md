# AAT Test Environment Script

Use `scripts/aat-env.sh` to export the BEFTA/test-runner environment needed for smoke and functional tests against AAT dependencies.

The script does not replace `.aat-remote-env`. `runRemoteAat` still uses `.aat-remote-env` for the local application runtime settings. The script is for the test JVM that runs `smoke` or `functional`.

## Prerequisites

Run from the repository root.

You need Azure CLI access to these Key Vaults:

```bash
ccd-aat
s2s-aat
```

You also need network access to AAT internal services when using internal URLs.

## Scenario 1: Local App With AAT Dependencies

Use this when debugging local code against AAT dependencies.

This is the recommended path for group-access debugging because deployed AAT has group access disabled.

Start the application in one terminal:

```bash
./gradlew runRemoteAat
```

Ensure `.aat-remote-env` enables group access for the local app:

```bash
grep '^ENABLE_CASE_GROUP_ACCESS=' .aat-remote-env
```

Expected:

```text
ENABLE_CASE_GROUP_ACCESS=true
```

Run tests in a second terminal:

```bash
source ./scripts/aat-env.sh
echo "$DEFINITION_STORE_URL_BASE"
echo "$GROUP_ACCESS_ENABLED"
./gradlew --stop
./gradlew --no-daemon smoke
./gradlew --no-daemon functional
```

Expected:

```text
DEFINITION_STORE_URL_BASE=http://localhost:4451
GROUP_ACCESS_ENABLED=true
```

## Scenario 2: Deployed AAT Target

Use this when you want to run tests against the deployed AAT/staging definition-store service rather than the local app.

`runRemoteAat` is not needed in this scenario.

```bash
CCD_DEF_STORE_TARGET=remote source ./scripts/aat-env.sh
echo "$DEFINITION_STORE_URL_BASE"
./gradlew --stop
./gradlew --no-daemon smoke
./gradlew --no-daemon functional
```

Expected:

```text
DEFINITION_STORE_URL_BASE=https://ccd-definition-store-api-staging.aat.platform.hmcts.net
```

Do not use this scenario for group-access debugging if deployed AAT has group access disabled.

## Useful Checks

Confirm BEFTA is using AAT IDAM API, not localhost or idam-web-public:

```bash
echo "$IDAM_API_URL_BASE"
```

Expected:

```text
https://idam-api.aat.platform.hmcts.net
```

Confirm BEFTA is using AAT S2S:

```bash
echo "$S2S_URL_BASE"
```

Expected:

```text
http://rpe-service-auth-provider-aat.service.core-compute-aat.internal
```

Confirm functional tests include group-access scenarios:

```bash
echo "$GROUP_ACCESS_ENABLED"
```

Expected:

```text
true
```

## Common Failures

`Connection refused ... localhost:5000/oauth2/authorize`

The test JVM is still using local IDAM. Source `scripts/aat-env.sh` in the same terminal used to run Gradle.

`301 Moved Permanently ... idam-web-public ... /oauth2/authorize`

`IDAM_API_URL_BASE` is wrong. It must be `https://idam-api.aat.platform.hmcts.net`.

`401 Unauthorized ... /oauth2/authorize`

The user password is wrong or stale. The script refreshes the relevant user passwords from `ccd-aat`.

`Connection refused ... localhost:4502/lease`

The test JVM is still using local S2S. Source `scripts/aat-env.sh` in the same terminal used to run Gradle.

`401 Unauthorized ... /lease`

The S2S key is wrong or stale. The script refreshes `microservicekey-ccd-gw` from `s2s-aat`.

`404 page not found` for `/api/user-role`

The target URL is probably wrong or not routed to definition-store. Check `DEFINITION_STORE_URL_BASE`.

`403` for `/import`

The request reached definition-store but authorization failed. Check that the importer user has `ccd-import` and that the target app accepts `ccd_gw` as an authorised S2S service.
