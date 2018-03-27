provider "vault" {
  address = "https://vault.reform.hmcts.net:6200"
}

locals {
  env_ase_url = "${var.env}.service.${data.terraform_remote_state.core_apps_compute.ase_name[0]}.internal"
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
    IDAM_S2S_URL = "${var.s2s_url}"
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
