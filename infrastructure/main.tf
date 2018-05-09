provider "vault" {
  address = "https://vault.reform.hmcts.net:6200"
}

locals {
  aseName = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"

  local_env = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"
  local_ase = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "core-compute-aat" : "core-compute-saat" : local.aseName}"

  env_ase_url = "${local.local_env}.service.${local.local_ase}.internal"

  s2s_url = "http://rpe-service-auth-provider-${local.env_ase_url}"

  previewVaultName = "${var.product}-${var.component}"
  # preview env contains pr number prefix, other envs need a suffix
  nonPreviewVaultName = "${local.previewVaultName}-${var.env}"
  vaultName = "${(var.env == "preview" || var.env == "spreview") ? local.previewVaultName : local.nonPreviewVaultName}"
}

data "vault_generic_secret" "definition_store_item_key" {
  path = "secret/${var.vault_section}/ccidam/service-auth-provider/api/microservice-keys/ccd-definition"
}

module "case-definition-store-api" {
  source   = "git@github.com:contino/moj-module-webapp?ref=master"
  product  = "${var.product}-definition-store-api"
  location = "${var.location}"
  env      = "${var.env}"
  ilbIp    = "${var.ilbIp}"
  subscription = "${var.subscription}"

  app_settings = {
    DEFINITION_STORE_DB_HOST = "${module.postgres-case-definition-store.host_name}"
    DEFINITION_STORE_DB_PORT = "${module.postgres-case-definition-store.postgresql_listen_port}"
    DEFINITION_STORE_DB_NAME = "${module.postgres-case-definition-store.postgresql_database}"
    DEFINITION_STORE_DB_USERNAME = "${module.postgres-case-definition-store.user_name}"
    DEFINITION_STORE_DB_PASSWORD = "${module.postgres-case-definition-store.postgresql_password}"
    IDAM_USER_URL = "${var.idam_api_url}"
    IDAM_S2S_URL = "${local.s2s_url}"
    DEFINITION_STORE_IDAM_KEY = "${data.vault_generic_secret.definition_store_item_key.data["value"]}"
    USER_PROFILE_HOST = "http://ccd-user-profile-api-${local.env_ase_url}"
  }

}

module "postgres-case-definition-store" {
  source              = "git@github.com:contino/moj-module-postgres?ref=master"
  product             = "${var.product}-definition-store"
  location            = "West Europe"
  env                 = "${var.env}"
  postgresql_user     = "ccd"
}

module "definition-store-vault" {
  source              = "git@github.com:hmcts/moj-module-key-vault?ref=master"
  name                = "${local.vaultName}" // Max 24 characters
  product             = "${var.product}"
  env                 = "${var.env}"
  tenant_id           = "${var.tenant_id}"
  object_id           = "${var.jenkins_AAD_objectId}"
  resource_group_name = "${module.case-definition-store-api.resource_group_name}"
  product_group_object_id = "be8b3850-998a-4a66-8578-da268b8abd6b"
}
