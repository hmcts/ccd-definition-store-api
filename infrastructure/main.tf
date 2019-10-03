provider "azurerm" {
  version = "1.22.1"
}

locals {
  app_full_name = "${var.product}-${var.component}"

  aseName = "core-compute-${var.env}"

  local_env = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"
  local_ase = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "core-compute-aat" : "core-compute-saat" : local.aseName}"

  env_ase_url = "${local.local_env}.service.${local.local_ase}.internal"

  s2s_url = "http://rpe-service-auth-provider-${local.env_ase_url}"

  // Vault name
  previewVaultName = "${var.raw_product}-aat"
  nonPreviewVaultName = "${var.raw_product}-${var.env}"
  vaultName = "${(var.env == "preview" || var.env == "spreview") ? local.previewVaultName : local.nonPreviewVaultName}"

  // Shared Resource Group
  previewResourceGroup = "${var.raw_product}-shared-aat"
  nonPreviewResourceGroup = "${var.raw_product}-shared-${var.env}"
  sharedResourceGroup = "${(var.env == "preview" || var.env == "spreview") ? local.previewResourceGroup : local.nonPreviewResourceGroup}"

  // Storage Account
  previewStorageAccountName = "${var.raw_product}sharedaat"
  nonPreviewStorageAccountName = "${var.raw_product}shared${var.env}"
  storageAccountName = "${(var.env == "preview" || var.env == "spreview") ? local.previewStorageAccountName : local.nonPreviewStorageAccountName}"

  sharedAppServicePlan = "${var.raw_product}-${var.env}"
  sharedASPResourceGroup = "${var.raw_product}-shared-${var.env}"

  custom_redirect_uri = "${var.frontend_url}/oauth2redirect"
  default_redirect_uri = "https://ccd-case-management-web-${local.env_ase_url}/oauth2redirect"
  oauth2_redirect_uri = "${var.frontend_url != "" ? local.custom_redirect_uri : local.default_redirect_uri}"

  elastic_search_host = "${var.elastic_search_enabled == "false" ? "" : "${join("", data.azurerm_key_vault_secret.ccd_elastic_search_url.*.value)}"}"
  elastic_search_password = "${var.elastic_search_enabled == "false" ? "" : "${join("", data.azurerm_key_vault_secret.ccd_elastic_search_password.*.value)}"}"
}

data "azurerm_key_vault" "ccd_shared_key_vault" {
  name = "${local.vaultName}"
  resource_group_name = "${local.sharedResourceGroup}"
}

data "azurerm_key_vault" "s2s_vault" {
  name = "s2s-${local.local_env}"
  resource_group_name = "rpe-service-auth-provider-${local.local_env}"
}

resource "azurerm_key_vault_secret" "ccd_definition_s2s_secret" {
  name = "ccd-definition-s2s-secret"
  value = "${data.azurerm_key_vault_secret.definition_store_s2s_secret.value}"
  key_vault_id = "${data.azurerm_key_vault.ccd_shared_key_vault.id}"
}

resource "azurerm_storage_container" "imports_container" {
  name = "${local.app_full_name}-imports-${var.env}"
  resource_group_name = "${local.sharedResourceGroup}"
  storage_account_name = "${local.storageAccountName}"
  container_access_type = "private"
}

data "azurerm_key_vault_secret" "definition_store_s2s_secret" {
  name = "microservicekey-ccd-definition"
  key_vault_id = "${data.azurerm_key_vault.s2s_vault.id}"
}

data "azurerm_key_vault_secret" "storageaccount_primary_connection_string" {
  name = "storage-account-primary-connection-string"
  key_vault_id = "${data.azurerm_key_vault.ccd_shared_key_vault.id}"
}

data "azurerm_key_vault_secret" "storageaccount_secondary_connection_string" {
  name = "storage-account-secondary-connection-string"
  key_vault_id = "${data.azurerm_key_vault.ccd_shared_key_vault.id}"
}

data "azurerm_key_vault_secret" "ccd_elastic_search_url" {
  count = "${var.elastic_search_enabled == "false" ? 0 : 1}"
  name = "ccd-ELASTIC-SEARCH-URL"
  key_vault_id = "${data.azurerm_key_vault.ccd_shared_key_vault.id}"
}

data "azurerm_key_vault_secret" "ccd_elastic_search_password" {
  count = "${var.elastic_search_enabled == "false" ? 0 : 1}"
  name = "ccd-ELASTIC-SEARCH-PASSWORD"
  key_vault_id = "${data.azurerm_key_vault.ccd_shared_key_vault.id}"
}

module "case-definition-store-api" {
  source   = "git@github.com:hmcts/cnp-module-webapp?ref=master"
  product  = "${local.app_full_name}"
  location = "${var.location}"
  appinsights_location = "${var.location}"
  env      = "${var.env}"
  ilbIp    = "${var.ilbIp}"
  subscription = "${var.subscription}"
  common_tags  = "${var.common_tags}"
  asp_name = "${(var.asp_name == "use_shared") ? local.sharedAppServicePlan : var.asp_name}"
  asp_rg = "${(var.asp_rg == "use_shared") ? local.sharedASPResourceGroup : var.asp_rg}"
  website_local_cache_sizeinmb = 1200
  capacity = "${var.capacity}"
  java_container_version = "9.0"
  appinsights_instrumentation_key = "${var.appinsights_instrumentation_key}"

  app_settings = {
    DEFINITION_STORE_DB_HOST = "${module.definition-store-db.host_name}"
    DEFINITION_STORE_DB_PORT = "${module.definition-store-db.postgresql_listen_port}"
    DEFINITION_STORE_DB_NAME = "${module.definition-store-db.postgresql_database}"
    DEFINITION_STORE_DB_USERNAME = "${module.definition-store-db.user_name}"
    DEFINITION_STORE_DB_PASSWORD = "${module.definition-store-db.postgresql_password}"
    DEFINITION_STORE_DB_OPTIONS = "?sslmode=require"
    DEFINITION_STORE_DB_MAX_POOL_SIZE = "${var.database_max_pool_size}"

    ENABLE_DB_MIGRATE = "false"

    IDAM_USER_URL = "${var.idam_api_url}"
    IDAM_S2S_URL = "${local.s2s_url}"
    DEFINITION_STORE_IDAM_KEY = "${data.azurerm_key_vault_secret.definition_store_s2s_secret.value}"

    DEFINITION_STORE_S2S_AUTHORISED_SERVICES = "${var.authorised-services}"

    USER_PROFILE_HOST = "http://ccd-user-profile-api-${local.env_ase_url}"

    // Storage Account
    AZURE_STORAGE_CONNECTION_STRING = "${data.azurerm_key_vault_secret.storageaccount_primary_connection_string.value}"
    AZURE_STORAGE_BLOB_CONTAINER_REFERENCE = "${azurerm_storage_container.imports_container.name}"
    AZURE_STORAGE_DEFINITION_UPLOAD_ENABLED = "true"

    ELASTIC_SEARCH_HOST = "${local.elastic_search_host}"
    ELASTIC_SEARCH_PASSWORD = "${local.elastic_search_password}"
    ELASTIC_SEARCH_PORT = "${var.elastic_search_port}"
    ELASTIC_SEARCH_SCHEME = "${var.elastic_search_scheme}"
    ELASTIC_SEARCH_ENABLED = "${var.elastic_search_enabled}"
    ELASTIC_SEARCH_INDEX_SHARDS = "${var.elastic_search_index_shards}"
    ELASTIC_SEARCH_INDEX_SHARDS_REPLICAS = "${var.elastic_search_index_shards_replicas}"
    ELASTIC_SEARCH_FAIL_ON_IMPORT = "${var.elastic_search_fail_on_import}"
    ELASTIC_SEARCH_DYNAMIC = "${var.elastic_search_dynamc}"
    ELASTIC_SEARCH_CASE_INDEX_NAME_FORMAT = "${var.elastic_search_case_index_name_format}"

    // Role-based authorization for CCD Admin Web
    ADMIN_WEB_AUTHORIZATION_ENABLED = "false" // Needs enabling once the appropriate roles are created in IdAM
    ADMIN_WEB_AUTHORIZATION_MANAGE_USER_PROFILE_0 = "ccd-import"
    ADMIN_WEB_AUTHORIZATION_MANAGE_USER_ROLE_0 = "ccd-import"
    ADMIN_WEB_AUTHORIZATION_MANAGE_DEFINITION_0 = "ccd-import"
    ADMIN_WEB_AUTHORIZATION_IMPORT_DEFINITION_0 = "ccd-import"
    // TODO More roles to be added to the appropriate actions, once they are created in IdAM

    CCD_AM_WRITE_TO_CCD_ONLY = "${var.ccd_am_write_to_ccd_only}"
    CCD_AM_WRITE_TO_AM_ONLY = "${var.ccd_am_write_to_am_only}"
    CCD_AM_WRITE_TO_BOTH = "${var.ccd_am_write_to_both}"
    CCD_AM_READ_FROM_CCD = "${var.ccd_am_read_from_ccd}"
    CCD_AM_READ_FROM_AM = "${var.ccd_am_read_from_am}"

  }
  common_tags = "${var.common_tags}"
}

module "definition-store-db" {
  source = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product = "${local.app_full_name}-postgres-db"
  location = "${var.location}"
  env = "${var.env}"
  subscription = "${var.subscription}"
  postgresql_user = "${var.postgresql_user}"
  database_name = "${var.database_name}"
  sku_name = "${var.database_sku_name}"
  sku_tier = "GeneralPurpose"
  sku_capacity = "${var.database_sku_capacity}"
  storage_mb = "51200"
  common_tags  = "${var.common_tags}"
}

////////////////////////////////
// Populate Vault with DB info
////////////////////////////////

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  name = "${var.component}-POSTGRES-USER"
  value = "${module.definition-store-db.user_name}"
  key_vault_id = "${data.azurerm_key_vault.ccd_shared_key_vault.id}"
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name = "${var.component}-POSTGRES-PASS"
  value = "${module.definition-store-db.postgresql_password}"
  key_vault_id = "${data.azurerm_key_vault.ccd_shared_key_vault.id}"
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  name = "${var.component}-POSTGRES-HOST"
  value = "${module.definition-store-db.host_name}"
  key_vault_id = "${data.azurerm_key_vault.ccd_shared_key_vault.id}"
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  name = "${var.component}-POSTGRES-PORT"
  value = "${module.definition-store-db.postgresql_listen_port}"
  key_vault_id = "${data.azurerm_key_vault.ccd_shared_key_vault.id}"
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  name = "${var.component}-POSTGRES-DATABASE"
  value = "${module.definition-store-db.postgresql_database}"
  key_vault_id = "${data.azurerm_key_vault.ccd_shared_key_vault.id}"
}
