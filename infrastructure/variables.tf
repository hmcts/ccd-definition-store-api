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

variable "aks_subscription_id" {}

variable "pgsql_storage_mb" {
  description = "Max storage allowed for the PGSql Flexibile instance"
  type        = number
  default     = 102400
}
