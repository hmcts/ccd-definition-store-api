variable "product" {
  type    = "string"
}

variable "component" {
  type    = "string"
}

variable "location" {
  type    = "string"
  default = "UK South"
}

variable "env" {
  type = "string"
}

variable "subscription" {
  type = "string"
}

variable "capacity" {
  default = "1"
}

variable "common_tags" {
  type = "map"
}

variable "ilbIp"{}

variable "tenant_id" {
  description = "(Required) The Azure Active Directory tenant ID that should be used for authenticating requests to the key vault. This is usually sourced from environemnt variables and not normally required to be specified."
}

variable "jenkins_AAD_objectId" {
  type = "string"
  description = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "vault_section" {
  default = "test"
}

variable "frontend_url" {
  type = "string"
  default = ""
  description = "Optional front end URL to use for building redirect URI"
}

////////////////////////////////
// Database
////////////////////////////////

variable "postgresql_user" {
  default = "ccd"
}

variable "database_name" {
  default = "ccd_definition_store"
}

////////////////////////////////
// IDAM
////////////////////////////////

variable "idam_api_url" {
  default = "http://betaDevBccidamAppLB.reform.hmcts.net"
}

////////////////////////////////
// S2S
////////////////////////////////

variable "authorised-services" {
  default = "ccd_data,ccd_gw,ccd_admin,jui_webapp"
}

////////////////////////////////
// ELASTIC SEARCH
////////////////////////////////

variable "elastic_search_host" {
  default = "localhost"
}

variable "elastic_search_port" {
  default = "9200"
}

variable "elastic_search_enabled" {
  default = "false"
}

variable "elastic_search_index_shards" {
  default = "3"
}

variable "elastic_search_index_shards_replicas" {
  default = "2"
}

variable "elastic_search_fail_on_import" {
  default = "false"
}

variable "elastic_search_dynamc" {
  default = "strict"
}

variable "elastic_search_case_index_name_format" {
  default = "%s_%s_cases"
}