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

kv_secret() {
  local vault_name="$1"
  local secret_name="$2"

  az keyvault secret show \
    --vault-name "${vault_name}" \
    --name "${secret_name}" \
    --query value \
    -o tsv | tr -d '\r\n'
}

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
export OAUTH2_CLIENT_SECRET="$(kv_secret ccd-aat ccd-api-gateway-oauth2-client-secret)"
export CCD_API_GATEWAY_OAUTH2_CLIENT_SECRET="${OAUTH2_CLIENT_SECRET}"

# S2S values used by BEFTA token generation.
export S2S_URL_BASE="http://rpe-service-auth-provider-aat.service.core-compute-aat.internal"
export S2S_URL="${S2S_URL_BASE}"
export IDAM_S2S_URL="${S2S_URL_BASE}"
export CCD_API_GATEWAY_S2S_ID="ccd_gw"
export CCD_API_GATEWAY_S2S_KEY="$(kv_secret s2s-aat microservicekey-ccd-gw)"
export BEFTA_S2S_CLIENT_ID="${CCD_API_GATEWAY_S2S_ID}"
export BEFTA_S2S_CLIENT_SECRET="${CCD_API_GATEWAY_S2S_KEY}"
export CCD_GW_SERVICE_NAME="${CCD_API_GATEWAY_S2S_ID}"
export CCD_GW_SERVICE_SECRET="${CCD_API_GATEWAY_S2S_KEY}"

# Users used by BEFTA data setup and smoke scenarios.
export CCD_CASEWORKER_AUTOTEST_EMAIL="auto.test.cnp@gmail.com"
export CCD_CASEWORKER_AUTOTEST_PASSWORD="$(kv_secret ccd-aat ccd-caseworker-autotest-password)"
export DEFINITION_IMPORTER_USERNAME="$(kv_secret ccd-aat definition-importer-username)"
export DEFINITION_IMPORTER_PASSWORD="$(kv_secret ccd-aat definition-importer-password)"
export CCD_BEFTA_CASEWORKER_3_PWD="$(kv_secret ccd-aat ccd-befta-caseworker-3-pwd)"
export CCD_BEFTA_MASTER_CASEWORKER_PWD="$(kv_secret ccd-aat ccd-befta-master-caseworker-pwd)"

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
