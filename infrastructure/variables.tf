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

variable "ilbIp"{}

variable "vault_section" {
  default = "test"
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

variable "use_uk_db" {
  type = "string"
  default = "false"
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

variable "s2s_url" {
  default = "http://betaDevBccidamS2SLB.reform.hmcts.net"
}


