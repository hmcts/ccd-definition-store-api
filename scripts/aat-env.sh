#!/usr/bin/env bash
#
# Source this before running definition-store smoke or functional tests against AAT dependencies.
#
# Local app run:
#   source ./scripts/aat-env.sh
#   ./gradlew runRemoteAat
#   ./gradlew --no-daemon smoke
#   ./gradlew --no-daemon functional
#
# Deployed AAT/staging target:
#   CCD_DEF_STORE_TARGET=remote source ./scripts/aat-env.sh
#   ./gradlew --no-daemon smoke
#   ./gradlew --no-daemon functional
#
# Requires Azure CLI access to ccd-aat and s2s-aat Key Vaults.

require_az_cli() {
  if ! command -v az >/dev/null 2>&1; then
    echo "ERROR: Azure CLI 'az' was not found on PATH." >&2
    echo "Install Azure CLI, then run 'az login' before sourcing this script." >&2
    return 1
  fi
}

require_azure_keyvault_token() {
  local account_output
  local token_output

  if ! account_output="$(az account show --query '{name:name, tenantId:tenantId, user:user.name}' -o json 2>&1)"; then
    echo "ERROR: Azure CLI is not logged in or cannot load the current account." >&2
    echo "${account_output}" >&2
    echo "Run 'az login', then retry: source ./scripts/aat-env.sh" >&2
    return 1
  fi

  if ! token_output="$(az account get-access-token --resource https://vault.azure.net --query expiresOn -o tsv 2>&1)"; then
    echo "ERROR: Azure CLI cannot get a Key Vault access token." >&2
    echo "${token_output}" >&2
    echo "If the error mentions login.microsoftonline.com, fix Azure CLI internet/proxy/VPN access first." >&2
    echo "Useful check: az account get-access-token --resource https://vault.azure.net -o table" >&2
    return 1
  fi
}

kv_secret() {
  local vault_name="$1"
  local secret_name="$2"
  local secret_value

  if ! secret_value="$(az keyvault secret show \
    --vault-name "${vault_name}" \
    --name "${secret_name}" \
    --query value \
    -o tsv)"; then
    return 1
  fi

  printf '%s' "${secret_value}" | tr -d '\r\n'
}

export_secret() {
  local variable_name="$1"
  local vault_name="$2"
  local secret_name="$3"
  local secret_value

  if ! secret_value="$(kv_secret "${vault_name}" "${secret_name}")"; then
    echo "ERROR: Failed to read '${secret_name}' from Key Vault '${vault_name}'." >&2
    echo "Confirm your Azure account has access to the AAT Key Vaults, then retry." >&2
    return 1
  fi

  if [ -z "${secret_value}" ]; then
    echo "ERROR: Secret '${secret_name}' from Key Vault '${vault_name}' returned an empty value." >&2
    return 1
  fi

  export "${variable_name}=${secret_value}"
}

require_az_cli || return 1 2>/dev/null || exit 1
require_azure_keyvault_token || return 1 2>/dev/null || exit 1

# IDAM: BEFTA /oauth2/authorize must use idam-api, not idam-web-public.
export IDAM_API_URL_BASE="https://idam-api.aat.platform.hmcts.net"
export IDAM_API_BASE_URL="${IDAM_API_URL_BASE}"
export IDAM_URL="${IDAM_API_URL_BASE}"
export IDAM_USER_URL="https://idam-web-public.aat.platform.hmcts.net"

# OAuth values used by BEFTA UserTokenProviderConfig.
export OAUTH2_CLIENT_ID="ccd_gateway"
export CCD_API_GATEWAY_OAUTH2_CLIENT_ID="${OAUTH2_CLIENT_ID}"
export OAUTH2_REDIRECT_URI="https://www-ccd.nonprod.platform.hmcts.net/oauth2redirect"
export CCD_API_GATEWAY_OAUTH2_REDIRECT_URL="${OAUTH2_REDIRECT_URI}"
export_secret OAUTH2_CLIENT_SECRET ccd-aat ccd-api-gateway-oauth2-client-secret \
  || return 1 2>/dev/null || exit 1
export CCD_API_GATEWAY_OAUTH2_CLIENT_SECRET="${OAUTH2_CLIENT_SECRET}"

# S2S values used by BEFTA token generation.
export S2S_URL_BASE="http://rpe-service-auth-provider-aat.service.core-compute-aat.internal"
export S2S_URL="${S2S_URL_BASE}"
export IDAM_S2S_URL="${S2S_URL_BASE}"
export CCD_API_GATEWAY_S2S_ID="ccd_gw"
export_secret CCD_API_GATEWAY_S2S_KEY s2s-aat microservicekey-ccd-gw \
  || return 1 2>/dev/null || exit 1
export BEFTA_S2S_CLIENT_ID="${CCD_API_GATEWAY_S2S_ID}"
export BEFTA_S2S_CLIENT_SECRET="${CCD_API_GATEWAY_S2S_KEY}"
export CCD_GW_SERVICE_NAME="${CCD_API_GATEWAY_S2S_ID}"
export CCD_GW_SERVICE_SECRET="${CCD_API_GATEWAY_S2S_KEY}"

# Users used by BEFTA data setup and smoke scenarios.
export CCD_CASEWORKER_AUTOTEST_EMAIL="auto.test.cnp@gmail.com"
export_secret CCD_CASEWORKER_AUTOTEST_PASSWORD ccd-aat ccd-caseworker-autotest-password \
  || return 1 2>/dev/null || exit 1
export_secret DEFINITION_IMPORTER_USERNAME ccd-aat definition-importer-username \
  || return 1 2>/dev/null || exit 1
export_secret DEFINITION_IMPORTER_PASSWORD ccd-aat definition-importer-password \
  || return 1 2>/dev/null || exit 1
export_secret CCD_BEFTA_CASEWORKER_3_PWD ccd-aat ccd-befta-caseworker-3-pwd \
  || return 1 2>/dev/null || exit 1
export_secret CCD_BEFTA_MASTER_CASEWORKER_PWD ccd-aat ccd-befta-master-caseworker-pwd \
  || return 1 2>/dev/null || exit 1

# Test target. Default is local app started with ./gradlew runRemoteAat.
case "${CCD_DEF_STORE_TARGET:-local}" in
  local)
    export TEST_URL="http://localhost:4451"
    export DEFINITION_STORE_URL_BASE="http://localhost:4451"
    ;;
  remote|staging|aat)
    export TEST_URL="https://ccd-definition-store-api-staging.aat.platform.hmcts.net"
    export DEFINITION_STORE_URL_BASE="https://ccd-definition-store-api-staging.aat.platform.hmcts.net"
    ;;
  *)
    echo "Unknown CCD_DEF_STORE_TARGET='${CCD_DEF_STORE_TARGET}'. Use 'local' or 'remote'." >&2
    return 1 2>/dev/null || exit 1
    ;;
esac
export DEFINITION_STORE_HOST="${DEFINITION_STORE_URL_BASE}"

# Jenkins-equivalent BEFTA knobs.
export BEFTA_RESPONSE_HEADER_CHECK_POLICY="JUST_WARN"
export BEFTA_RETRY_MAX_ATTEMPTS="3"
export BEFTA_RETRY_STATUS_CODES="500,502,503,504"
export BEFTA_RETRY_MAX_DELAY="1000"
export BEFTA_RETRY_NON_RETRYABLE_HTTP_METHODS="PUT,POST"
export DEFAULT_COLLECTION_ASSERTION_MODE="UNORDERED"
export CCD_STUB_SERVICE_URI_BASE="ccd-test-stubs-service-aat.service.core-compute-aat.internal"
export GROUP_ACCESS_ENABLED="${GROUP_ACCESS_ENABLED:-true}"

echo "AAT environment exported:"
echo "  IDAM_API_URL_BASE=${IDAM_API_URL_BASE}"
echo "  S2S_URL_BASE=${S2S_URL_BASE}"
echo "  OAUTH2_CLIENT_ID=${OAUTH2_CLIENT_ID}"
echo "  OAUTH2_REDIRECT_URI=${OAUTH2_REDIRECT_URI}"
echo "  DEFINITION_STORE_URL_BASE=${DEFINITION_STORE_URL_BASE}"
