provider "azurerm" {
  features {
    resource_group {
      prevent_deletion_if_contains_resources = false
    }
  }
}

locals {
  app_full_name = "${var.product}-${var.component}"

  // Vault name
  vaultName = "${var.raw_product}-${var.env}"

  // Shared Resource Group
  sharedResourceGroup = "${var.raw_product}-shared-${var.env}"

  // Storage Account
  storageAccountName = "${var.raw_product}shared${var.env}"

  db_name = "${local.app_full_name}-postgres-db-v15"

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

///////////////////////
// Postgres DB info  //
///////////////////////

module "postgresql_v15" {
  source = "git@github.com:hmcts/terraform-module-postgresql-flexible?ref=master"
  providers = {
    azurerm.postgres_network = azurerm.postgres_network
  }
  
  admin_user_object_id = var.jenkins_AAD_objectId
  business_area        = "cft"
  common_tags          = var.common_tags
  component            = var.component
  env                  = var.env
  subnet_suffix        = var.subnet_suffix
  force_user_permissions_trigger = "1"
  pgsql_databases = [
    {
      name = var.database_name
    }
  ]
  pgsql_server_configuration = [
    {
      name  = "azure.extensions"
      value = "pg_stat_statements,pg_buffercache,hypopg"
    }
  ]
  pgsql_version               = "15"
  product                     = var.product
  name                        = local.db_name
  pgsql_sku                   = var.pgsql_sku
  pgsql_storage_mb            = var.pgsql_storage_mb
  action_group_name           = join("-", [var.action_group_name, local.db_name, var.env])
  email_address_key           = var.email_address_key
  email_address_key_vault_id  = data.azurerm_key_vault.ccd_shared_key_vault.id
}

////////////////////////////////////
// Populate KeyVault with DB info //
////////////////////////////////////

resource "azurerm_key_vault_secret" "POSTGRES-USER-V15" {
  name         = "${var.component}-POSTGRES-USER-V15"
  value        = module.postgresql_v15.username
  key_vault_id = data.azurerm_key_vault.ccd_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS-V15" {
  name         = "${var.component}-POSTGRES-PASS-V15"
  value        = module.postgresql_v15.password
  key_vault_id = data.azurerm_key_vault.ccd_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-HOST-V15" {
  name         = "${var.component}-POSTGRES-HOST-V15"
  value        = module.postgresql_v15.fqdn
  key_vault_id = data.azurerm_key_vault.ccd_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  name         = "${var.component}-POSTGRES-PORT"
  value        = "5432"
  key_vault_id = data.azurerm_key_vault.ccd_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  name         = "${var.component}-POSTGRES-DATABASE"
  value        = var.database_name
  key_vault_id = data.azurerm_key_vault.ccd_shared_key_vault.id
}
