#!/usr/bin/env bash
set -u

env_name="${1:-aat}"
env_file=".${env_name}-remote-env"
run_task="runRemote$(printf '%s' "${env_name}" | awk '{print toupper(substr($0,1,1)) substr($0,2)}')"

if [ ! -f "${env_file}" ]; then
  echo "ERROR: ${env_file} does not exist." >&2
  echo "Create it first: ./gradlew reloadEnvSecrets -Penv=${env_name}" >&2
  exit 1
fi

set -a
# shellcheck disable=SC1090
. "./${env_file}"
set +a

status=0

host_from_url() {
  printf '%s' "$1" | sed -E 's#^[a-zA-Z][a-zA-Z0-9+.-]*://##; s#/.*$##; s#:.*$##'
}

port_from_url() {
  local url="$1"
  local default_port="$2"
  local host_port
  host_port="$(printf '%s' "${url}" | sed -E 's#^[a-zA-Z][a-zA-Z0-9+.-]*://##; s#/.*$##')"
  case "${host_port}" in
    *:*) printf '%s' "${host_port##*:}" ;;
    *) printf '%s' "${default_port}" ;;
  esac
}

check_tcp() {
  local label="$1"
  local host="$2"
  local port="$3"

  if [ -z "${host}" ] || [ -z "${port}" ]; then
    echo "FAIL ${label}: missing host or port" >&2
    status=1
    return
  fi

  if ! dscacheutil -q host -a name "${host}" >/dev/null 2>&1 && ! nslookup "${host}" >/dev/null 2>&1; then
    echo "FAIL ${label}: cannot resolve ${host}" >&2
    status=1
    return
  fi

  if nc -G 5 -z "${host}" "${port}" >/dev/null 2>&1 || nc -w 5 -z "${host}" "${port}" >/dev/null 2>&1; then
    echo "OK   ${label}: ${host}:${port}"
  else
    echo "FAIL ${label}: cannot connect to ${host}:${port}" >&2
    status=1
  fi
}

check_url() {
  local label="$1"
  local url="$2"
  local default_port="$3"

  [ -n "${url}" ] || return
  check_tcp "${label}" "$(host_from_url "${url}")" "$(port_from_url "${url}" "${default_port}")"
}

check_tcp "database" "${DEFINITION_STORE_DB_HOST:-}" "${DEFINITION_STORE_DB_PORT:-5432}"
check_url "s2s" "${IDAM_S2S_URL:-${S2S_URL_BASE:-}}" 80
check_url "idam api" "${IDAM_API_URL_BASE:-${IDAM_USER_URL:-}}" 443
check_url "idam web" "${IDAM_URL:-}" 443
check_url "translation service" "${TS_TRANSLATION_SERVICE_HOST:-}" 80
check_url "user profile" "${USER_PROFILE_HOST:-}" 80

if [ "${status}" -ne 0 ]; then
  echo
  echo "One or more remote dependencies are unreachable. Check VPN/bastion/DNS before running ${run_task}." >&2
fi

exit "${status}"
