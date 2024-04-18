variable "product" {
}

variable "raw_product" {
  default = "ccd" // jenkins-library overrides product for PRs and adds e.g. pr-118-ccd
}

variable "component" {
}

variable "location" {
  default = "UK South"
}

variable "env" {
}

variable "subscription" {
}

variable "common_tags" {
  type = map(string)
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

variable "database_sku_name" {
  default = "GP_Gen5_2"
}

variable "database_sku_capacity" {
  default = "2"
}

variable "pgsql_sku" {
  description = "The PGSql flexible server instance sku"
  default     = "GP_Standard_D2s_v3"
}

variable "jenkins_AAD_objectId" {
  description = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "aks_subscription_id" {}

variable "pgsql_storage_mb" {
  description = "Max storage allowed for the PGSql Flexibile instance"
  type        = number
  default     = 102400
}

variable "subnet_suffix" {
  default     = null
  type        = string
  description = "Suffix to append to the subnet name, the originally created one used by this module is full in a number of environments."
}

variable "enable_schema_ownership" {
  type        = bool
  default     = false
  description = "Enables the schema ownership script. Change this to true if you want to use the script. Defaults to false"
}

variable "force_schema_ownership_trigger" {
  default     = ""
  type        = string
  description = "Update this to a new value to force the schema ownership script to run again."
}

variable "kv_subscription" {
  default     = "DCD-CNP-DEV"
  type        = string
  description = "Update this with the name of the subscription where the single server key vault is. Defaults to DCD-CNP-DEV."

}
variable "tenant_id" {
  description = "(Required) The Azure Active Directory tenant ID that should be used for authenticating requests to the key vault. This is usually sourced from environment variables and not normally required to be specified."
}