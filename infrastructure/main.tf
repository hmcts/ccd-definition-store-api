provider "azurerm" {
  features {}
}

locals {
  app_full_name = "${var.product}-${var.component}"

  // Vault name
  vaultName = "${var.raw_product}-${var.env}"

  // Shared Resource Group
  sharedResourceGroup = "${var.raw_product}-shared-${var.env}"

  // Storage Account
  storageAccountName = "${var.raw_product}shared${var.env}"

}

data "azurerm_key_vault" "ccd_shared_key_vault" {
  name                = "${local.vaultName}"
  resource_group_name = "${local.sharedResourceGroup}"
}

data "azurerm_key_vault" "s2s_vault" {
  name                = "s2s-${var.env}"
  resource_group_name = "rpe-service-auth-provider-${var.env}"
}

resource "azurerm_key_vault_secret" "ccd_definition_s2s_secret" {
  name         = "ccd-definition-s2s-secret"
  value        = data.azurerm_key_vault_secret.definition_store_s2s_secret.value
  key_vault_id = data.azurerm_key_vault.ccd_shared_key_vault.id
}

resource "azurerm_storage_container" "imports_container" {
  name                  = "${local.app_full_name}-imports-${var.env}"
  storage_account_name  = local.storageAccountName
  container_access_type = "private"
}

data "azurerm_key_vault_secret" "definition_store_s2s_secret" {
  name         = "microservicekey-ccd-definition"
  key_vault_id = "${data.azurerm_key_vault.s2s_vault.id}"
}

module "definition-store-db" {
  source          = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product         = var.product
  component       = var.component
  name            = "${local.app_full_name}-postgres-db"
  location        = "${var.location}"
  env             = "${var.env}"
  subscription    = "${var.subscription}"
  postgresql_user = "${var.postgresql_user}"
  database_name   = "${var.database_name}"
  sku_name        = "${var.database_sku_name}"
  sku_tier        = "GeneralPurpose"
  sku_capacity    = "${var.database_sku_capacity}"
  storage_mb      = "51200"
  common_tags     = "${var.common_tags}"
}

////////////////////////////////
// Populate Vault with DB info
////////////////////////////////

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  name         = "${var.component}-POSTGRES-USER"
  value        = module.definition-store-db.user_name
  key_vault_id = data.azurerm_key_vault.ccd_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name         = "${var.component}-POSTGRES-PASS"
  value        = module.definition-store-db.postgresql_password
  key_vault_id = data.azurerm_key_vault.ccd_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  name         = "${var.component}-POSTGRES-HOST"
  value        = module.definition-store-db.host_name
  key_vault_id = data.azurerm_key_vault.ccd_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  name         = "${var.component}-POSTGRES-PORT"
  value        = module.definition-store-db.postgresql_listen_port
  key_vault_id = data.azurerm_key_vault.ccd_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  name         = "${var.component}-POSTGRES-DATABASE"
  value        = module.definition-store-db.postgresql_database
  key_vault_id = data.azurerm_key_vault.ccd_shared_key_vault.id
}


////////////////////////////////
// DB version 11              //
////////////////////////////////

module "definition-store-dbv11" {
  source          = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product         = "${var.component}-db-v11"
  component       = var.component
  name            = "${local.app_full_name}-postgres-db-v11"
  location        = "${var.location}"
  env             = "${var.env}"
  subscription    = "${var.subscription}"
  postgresql_user = "${var.postgresql_user}"
  database_name   = "${var.database_name}"
  postgresql_version = "11"
  sku_name        = "GP_Gen5_2"
  sku_tier        = "GeneralPurpose"
  storage_mb      = "51200"
  common_tags     = "${var.common_tags}"
}

resource "azurerm_key_vault_secret" "POSTGRES-USER-V11" {
  name         = "${var.component}-POSTGRES-USER-V11"
  value        = module.definition-store-dbv11.user_name
  key_vault_id = data.azurerm_key_vault.ccd_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS-V11" {
  name         = "${var.component}-POSTGRES-PASS-V11"
  value        = module.definition-store-dbv11.postgresql_password
  key_vault_id = data.azurerm_key_vault.ccd_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST-V11" {
  name         = "${var.component}-POSTGRES-HOST-V11"
  value        = module.definition-store-dbv11.host_name
  key_vault_id = data.azurerm_key_vault.ccd_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT-V11" {
  name         = "${var.component}-POSTGRES-PORT-V11"
  value        = module.definition-store-dbv11.postgresql_listen_port
  key_vault_id = data.azurerm_key_vault.ccd_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE-V11" {
  name         = "${var.component}-POSTGRES-DATABASE-V11"
  value        = module.definition-store-dbv11.postgresql_database
  key_vault_id = data.azurerm_key_vault.ccd_shared_key_vault.id
}
