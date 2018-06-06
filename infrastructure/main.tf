provider "vault" {
  address = "https://vault.reform.hmcts.net:6200"
}

locals {
  app_full_name = "${var.product}-${var.component}"

  aseName = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"

  local_env = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"
  local_ase = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "core-compute-aat" : "core-compute-saat" : local.aseName}"

  env_ase_url = "${local.local_env}.service.${local.local_ase}.internal"

  s2s_url = "http://rpe-service-auth-provider-${local.env_ase_url}"

  // Vault name
  previewVaultName = "${var.product}-definition"
  # preview env contains pr number prefix, other envs need a suffix
  nonPreviewVaultName = "${local.previewVaultName}-${var.env}"
  vaultName = "${(var.env == "preview" || var.env == "spreview") ? local.previewVaultName : local.nonPreviewVaultName}"

  // Vault URI
  previewVaultUri = "https://ccd-definition-aat.vault.azure.net/"
  nonPreviewVaultUri = "${module.definition-store-vault.key_vault_uri}"
  vaultUri = "${(var.env == "preview" || var.env == "spreview") ? local.previewVaultUri : local.nonPreviewVaultUri}"

  custom_redirect_uri = "${var.frontend_url}/oauth2redirect"
  default_redirect_uri = "https://ccd-case-management-web-${local.env_ase_url}/oauth2redirect"
  oauth2_redirect_uri = "${var.frontend_url != "" ? local.custom_redirect_uri : local.default_redirect_uri}"
}

data "vault_generic_secret" "definition_store_item_key" {
  path = "secret/${var.vault_section}/ccidam/service-auth-provider/api/microservice-keys/ccd-definition"
}

data "vault_generic_secret" "gateway_idam_key" {
  path = "secret/${var.vault_section}/ccidam/service-auth-provider/api/microservice-keys/ccd-gw"
}

data "vault_generic_secret" "gateway_oauth2_client_secret" {
  path = "secret/${var.vault_section}/ccidam/idam-api/oauth2/client-secrets/ccd-gateway"
}

module "case-definition-store-api" {
  source   = "git@github.com:contino/moj-module-webapp?ref=master"
  product  = "${local.app_full_name}"
  location = "${var.location}"
  env      = "${var.env}"
  ilbIp    = "${var.ilbIp}"
  subscription = "${var.subscription}"

  app_settings = {
    DEFINITION_STORE_DB_HOST = "${var.use_uk_db != "true" ? module.postgres-case-definition-store.host_name : module.definition-store-db.host_name}"
    DEFINITION_STORE_DB_PORT = "${var.use_uk_db != "true" ? module.postgres-case-definition-store.postgresql_listen_port : module.definition-store-db.postgresql_listen_port}"
    DEFINITION_STORE_DB_NAME = "${var.use_uk_db != "true" ? module.postgres-case-definition-store.postgresql_database : module.definition-store-db.postgresql_database}"
    DEFINITION_STORE_DB_USERNAME = "${var.use_uk_db != "true" ? module.postgres-case-definition-store.user_name : module.definition-store-db.user_name}"
    DEFINITION_STORE_DB_PASSWORD = "${var.use_uk_db != "true" ? module.postgres-case-definition-store.postgresql_password : module.definition-store-db.postgresql_password}"

    UK_DB_HOST = "${module.definition-store-db.host_name}"
    UK_DB_PORT = "${module.definition-store-db.postgresql_listen_port}"
    UK_DB_NAME = "${module.definition-store-db.postgresql_database}"
    UK_DB_USERNAME = "${module.definition-store-db.user_name}"
    UK_DB_PASSWORD = "${module.definition-store-db.postgresql_password}"

    ENABLE_DB_MIGRATE = "false"

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

module "definition-store-db" {
  source = "git@github.com:hmcts/moj-module-postgres?ref=cnp-449-tactical"
  product = "${local.app_full_name}-postgres-db"
  location = "${var.location}"
  env = "${var.env}"
  postgresql_user = "${var.postgresql_user}"
  database_name = "${var.database_name}"
  sku_name = "GP_Gen5_2"
  sku_tier = "GeneralPurpose"
  storage_mb = "51200"
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

////////////////////////////////
// Populate Vault with DB info
////////////////////////////////

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  name = "${local.app_full_name}-POSTGRES-USER"
  value = "${var.use_uk_db != "true" ? module.postgres-case-definition-store.user_name : module.definition-store-db.user_name}"
  vault_uri = "${module.definition-store-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name = "${local.app_full_name}-POSTGRES-PASS"
  value = "${var.use_uk_db != "true" ? module.postgres-case-definition-store.postgresql_password : module.definition-store-db.postgresql_password}"
  vault_uri = "${module.definition-store-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  name = "${local.app_full_name}-POSTGRES-HOST"
  value = "${var.use_uk_db != "true" ? module.postgres-case-definition-store.host_name : module.definition-store-db.host_name}"
  vault_uri = "${module.definition-store-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  name = "${local.app_full_name}-POSTGRES-PORT"
  value = "${var.use_uk_db != "true" ? module.postgres-case-definition-store.postgresql_listen_port : module.definition-store-db.postgresql_listen_port}"
  vault_uri = "${module.definition-store-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  name = "${local.app_full_name}-POSTGRES-DATABASE"
  value = "${var.use_uk_db != "true" ? module.postgres-case-definition-store.postgresql_database : module.definition-store-db.postgresql_database}"
  vault_uri = "${module.definition-store-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "gw_s2s_key" {
  name = "microserviceGatewaySecret"
  value = "${data.vault_generic_secret.gateway_idam_key.data["value"]}"
  vault_uri = "${module.definition-store-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "gw_oauth2_secret" {
  name = "gatewayOAuth2ClientSecret"
  value = "${data.vault_generic_secret.gateway_oauth2_client_secret.data["value"]}"
  vault_uri = "${module.definition-store-vault.key_vault_uri}"
}
