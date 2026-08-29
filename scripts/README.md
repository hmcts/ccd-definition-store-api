# AAT Test Environment Script

Use `scripts/aat-env.sh` to export the BEFTA/test-runner environment needed for smoke and functional tests against AAT dependencies.

The script does not replace `.aat-remote-env`. `runRemoteAat` uses `.aat-remote-env` for the local application
runtime settings. `scripts/aat-env.sh` is for the test JVM that runs `smoke` or `functional`.

## Prerequisites

Run from the repository root.

| Requirement | Notes |
|-------------|-------|
| Azure CLI access | Required for `ccd-aat` and `s2s-aat` Key Vault secrets. |
| Non-prod bastion access | Required before connecting to the F5 VPN. |
| F5 VPN connection | Required before running local smoke or functional tests against AAT dependencies. |
| AAT internal network access | Required when using internal service URLs. |

## Scenario 1: Local App With AAT Dependencies

Use this when debugging local code or local runtime flags against AAT dependencies.

This is the recommended path when you need the local application code or local runtime settings while still using AAT
dependencies. The relevant settings may include group access, Elasticsearch, import timeout, another feature flag, or
no special feature flag.

Remote environment files follow the pattern `.<env>-remote-env`. The `env` value maps to the Key Vault name
`ccd-<env>` and the secret `definition-store-remote-env`.

```bash
az login
./gradlew reloadEnvSecrets -Penv=<env>
```

Examples:

```bash
./gradlew reloadEnvSecrets -Penv=aat
./gradlew reloadEnvSecrets -Penv=demo
```

| Environment | File created | Run task |
|-------------|--------------|----------|
| `aat` | `.aat-remote-env` | `./gradlew runRemoteAat` |
| `demo` | `.demo-remote-env` | `./gradlew runRemoteDemo` |

These files are ignored by Git because they contain local runtime settings and secrets. If you start the application
before creating the file, the remote run task calls the Gradle `configRemoteRunTask` helper internally. That helper tries
to create the matching remote-env file from Key Vault before launching the app. To add another environment, add a
matching `runRemote<Env>` task that calls `configRemoteRunTask(it, '<env>')`.

If Key Vault generation is not available, create `.aat-remote-env` manually with plain `KEY=value` lines. Do not add
comments or blank lines because `runRemoteAat` loads every line as an environment variable.

Example. Replace every value in angle brackets before starting the app. Generated files may contain extra variables; keep
them if present.

```text
APPINSIGHTS_INSTRUMENTATIONKEY=
AZURE_STORAGE_CONNECTION_STRING=
DEFINITION_STORE_DB_NAME=<aat database name>
DEFINITION_STORE_DB_HOST=<aat database host>
DEFINITION_STORE_DB_PASSWORD=<database password>
DEFINITION_STORE_DB_OPTIONS=?sslmode=require
DEFINITION_STORE_DB_PORT=5432
DEFINITION_STORE_DB_USERNAME=<database username>
DEFINITION_STORE_IDAM_KEY=<definition store idam key>
ELASTIC_SEARCH_PORT=9200
IDAM_S2S_URL=http://rpe-service-auth-provider-aat.service.core-compute-aat.internal
IDAM_API_URL_BASE=https://idam-api.aat.platform.hmcts.net
IDAM_USER_URL=https://idam-api.aat.platform.hmcts.net
IDAM_URL=https://idam-api.aat.platform.hmcts.net
OIDC_ISSUER=https://idam-web-public.aat.platform.hmcts.net/o
TS_TRANSLATION_SERVICE_HOST=<translation service url>
USER_PROFILE_HOST=<user profile service url>
CCD_API_GATEWAY_S2S_KEY=<ccd_gw s2s key>
CCD_API_GATEWAY_S2S_ID=ccd_gw
CCD_API_GATEWAY_OAUTH2_CLIENT_SECRET=<ccd gateway oauth2 client secret>
S2S_URL_BASE=http://rpe-service-auth-provider-aat.service.core-compute-aat.internal
OAUTH2_CLIENT_ID=ccd_gateway
OAUTH2_CLIENT_SECRET=<ccd gateway oauth2 client secret>
OAUTH2_REDIRECT_URI=https://www-ccd.nonprod.platform.hmcts.net/oauth2redirect
ENABLE_CASE_GROUP_ACCESS=true
DEFINITION_STORE_TX_TIMEOUT_DEFAULT=900
CCD_TX_TIMEOUT_DEFAULT=900
SPRING_DATASOURCE_HIKARI_KEEPALIVE_TIME=30000
SPRING_DATASOURCE_HIKARI_MAX_LIFETIME=120000
SPRING_DATASOURCE_HIKARI_IDLE_TIMEOUT=10000
SPRING_DATASOURCE_HIKARI_VALIDATION_TIMEOUT=5000
SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT=30000
SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE=0
SPRING_JPA_PROPERTIES_HIBERNATE_JDBC_BATCH_SIZE=25
```

| Setting | Purpose |
|---------|---------|
| `APPINSIGHTS_INSTRUMENTATIONKEY` | Optional local telemetry key. Leave blank unless needed. |
| `AZURE_STORAGE_CONNECTION_STRING` | Optional Azure Storage connection string for import archive upload. Leave blank unless needed. |
| `DEFINITION_STORE_DB_*` | Points the local app at the AAT definition-store database. |
| `IDAM_*`, `OIDC_ISSUER` | Points authentication and issuer validation at AAT IDAM. |
| `S2S_URL_BASE`, `IDAM_S2S_URL` | Points service-to-service token checks at AAT S2S. |
| `CCD_API_GATEWAY_*`, `OAUTH2_*` | Provides gateway S2S and OAuth2 credentials used by the local app. |
| `ELASTIC_SEARCH_PORT` | Sets the Elasticsearch port used by the local app. |
| `TS_TRANSLATION_SERVICE_HOST`, `USER_PROFILE_HOST` | Points supporting service clients at AAT-compatible services. |
| `ENABLE_CASE_GROUP_ACCESS` | Enables group access processing in the local app. Must be `true` before importing AccessType definitions. |
| `DEFINITION_STORE_TX_TIMEOUT_DEFAULT`, `CCD_TX_TIMEOUT_DEFAULT` | Raises transaction timeout for slow remote-database imports. |
| `SPRING_DATASOURCE_HIKARI_*` | Reduces stale remote DB connections while debugging over VPN. |
| `SPRING_JPA_PROPERTIES_HIBERNATE_JDBC_BATCH_SIZE` | Reduces import batch size if remote DB writes are unstable. |

Before starting the application, check the local runtime settings in `.aat-remote-env` that are relevant to your test.
For example:

```bash
grep '^ENABLE_CASE_GROUP_ACCESS=' .aat-remote-env
grep '^DEFINITION_STORE_TX_TIMEOUT_DEFAULT=' .aat-remote-env
```

Expected for the current group-access and long-running import debugging run:

```text
ENABLE_CASE_GROUP_ACCESS=true
DEFINITION_STORE_TX_TIMEOUT_DEFAULT=900
```

Before starting the app, check DNS and TCP connectivity to the remote dependencies:

```bash
./scripts/check-remote-env.sh aat
```

Start the application in one terminal:

```bash
./gradlew runRemoteAat
```

Run tests in a second terminal:

```bash
az login
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
export CCD_DEF_STORE_TARGET=remote
source ./scripts/aat-env.sh
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

| Symptom | Likely cause | Fix |
|---------|--------------|-----|
| `Connection refused ... localhost:5000/oauth2/authorize` | The test JVM is still using local IDAM. | Source `scripts/aat-env.sh` in the same terminal used to run Gradle. |
| `301 Moved Permanently ... idam-web-public ... /oauth2/authorize` | `IDAM_API_URL_BASE` is wrong. | It must be `https://idam-api.aat.platform.hmcts.net`. |
| `401 Unauthorized ... /oauth2/authorize` | The user password is wrong or stale. | Re-source `scripts/aat-env.sh`; it refreshes the relevant user passwords from `ccd-aat`. |
| `Connection refused ... localhost:4502/lease` | The test JVM is still using local S2S. | Source `scripts/aat-env.sh` in the same terminal used to run Gradle. |
| `UnknownHostException: rpe-service-auth-provider-aat.service.core-compute-aat.internal` | The local app or test JVM cannot resolve internal AAT S2S DNS. | Connect to bastion and F5 VPN, then run `./scripts/check-remote-env.sh aat` before `./gradlew runRemoteAat`. |
| `401 Unauthorized ... /lease` | The S2S key is wrong or stale. | Re-source `scripts/aat-env.sh`; it refreshes `microservicekey-ccd-gw` from `s2s-aat`. |
| `404 page not found` for `/api/user-role` | The target URL is probably wrong or not routed to definition-store. | Check `DEFINITION_STORE_URL_BASE`. |
| `403` for `/import` | The request reached definition-store but authorization failed. | Check that the importer user has `ccd-import` and that the target app accepts `ccd_gw` as an authorised S2S service. |
| `500` for `/import` with `transaction timeout expired` | The local app completed authentication but the import transaction exceeded Spring's timeout. | For local AAT debugging, set `DEFINITION_STORE_TX_TIMEOUT_DEFAULT=900` and `CCD_TX_TIMEOUT_DEFAULT=900` in `.aat-remote-env`, then restart `./gradlew runRemoteAat`. |
| `JDBCConnectionException` with `An I/O error occurred while sending to the backend` | The local app is talking to remote Azure Postgres and may have a stale or unstable DB connection. | For local AAT debugging, add the Hikari settings below to `.aat-remote-env`, then restart `./gradlew runRemoteAat`. |
| `409 Conflict` for `/import` with `unique_field_type_reference_version_jurisdiction` | The import reached the DB but collided with existing definition rows for the same field type, version, and jurisdiction. This is usually shared AAT DB state from repeated, partial, or concurrent imports, and can mask the validation error a negative scenario expects. | Stop any duplicate local app or test runs. Use clean AAT test data, wait for the shared test state to be reset, or rerun in an isolated environment before treating the scenario as a code failure. |
| `UnknownHostException: hmcts.github.io` in `F-125` | The Swagger spec scenario calls public GitHub Pages, and public DNS or internet access is not available from the test JVM. | Fix DNS/proxy/VPN routing for public internet access, or exclude F-125 when it is outside the local AAT debugging scope: `./gradlew --no-daemon functional -Ptags="not @F-125"`. |

Hikari timeout values are in milliseconds.

| Setting | Value | Reason |
|---------|-------|--------|
| `SPRING_DATASOURCE_HIKARI_KEEPALIVE_TIME` | `30000` | Validates idle connections if the pool keeps any, so broken sockets can be discarded. |
| `SPRING_DATASOURCE_HIKARI_MAX_LIFETIME` | `120000` | Retires connections quickly during remote-debug runs instead of keeping long-lived Azure Postgres sockets. |
| `SPRING_DATASOURCE_HIKARI_IDLE_TIMEOUT` | `10000` | Removes idle connections quickly so the next import is less likely to reuse a stale socket. |
| `SPRING_DATASOURCE_HIKARI_VALIDATION_TIMEOUT` | `5000` | Limits how long Hikari waits when checking whether a connection is alive. |
| `SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT` | `30000` | Gives the local app longer to obtain a fresh remote DB connection over VPN. |
| `SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE` | `0` | Allows the pool to drain idle connections instead of keeping remote DB sockets open. |

If the failure continues while flushing Hibernate batch inserts, reduce the remote-debug batch size:

```text
SPRING_JPA_PROPERTIES_HIBERNATE_JDBC_BATCH_SIZE=25
```

If Spring does not apply the environment variable form, use `SPRING_APPLICATION_JSON` in `.aat-remote-env` to set
`spring.jpa.properties.hibernate.jdbc.batch_size` explicitly.

Example `SPRING_APPLICATION_JSON` value:

```json
{
  "ccd": {
    "tx-timeout": {
      "default": "900"
    }
  },
  "spring": {
    "datasource": {
      "hikari": {
        "connection-timeout": "30000",
        "idle-timeout": "10000",
        "keepalive-time": "30000",
        "max-lifetime": "120000",
        "minimum-idle": "0",
        "validation-timeout": "5000"
      }
    },
    "jpa": {
      "properties": {
        "hibernate.jdbc.batch_size": "25"
      }
    }
  }
}
```

In `.aat-remote-env`, keep the value on one line:

```text
SPRING_APPLICATION_JSON={"ccd":{"tx-timeout":{"default":"900"}},"spring":{"datasource":{"hikari":{"connection-timeout":"30000","idle-timeout":"10000","keepalive-time":"30000","max-lifetime":"120000","minimum-idle":"0","validation-timeout":"5000"}},"jpa":{"properties":{"hibernate.jdbc.batch_size":"25"}}}}
```
