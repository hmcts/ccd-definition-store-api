variable "product" {
  type    = "string"
}

variable "raw_product" {
  default = "ccd" // jenkins-library overrides product for PRs and adds e.g. pr-118-ccd
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

variable "asp_name" {
  type = "string"
  description = "App Service Plan (ASP) to use for the webapp, 'use_shared' to make use of the shared ASP"
  default = "use_shared"
}

variable "asp_rg" {
  type = "string"
  description = "App Service Plan (ASP) resource group for 'asp_name', 'use_shared' to make use of the shared resource group"
  default = "use_shared"
}

variable "tenant_id" {
  description = "(Required) The Azure Active Directory tenant ID that should be used for authenticating requests to the key vault. This is usually sourced from environemnt variables and not normally required to be specified."
}

variable "jenkins_AAD_objectId" {
  type = "string"
  description = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
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
  default = "ccd_data,ccd_gw,ccd_admin,jui_webapp,pui_webapp"
}

// TAG SPECIFIC VARIABLES
variable "team_name" {
  type        = "string"
  description = "The name of your team"
  default     = "CDM (CCD/DM)"
}

variable "team_contact" {
  type        = "string"
  description = "The name of your Slack channel people can use to contact your team about your infrastructure"
  default     = "#ccd-devops"
}

variable "destroy_me" {
  type        = "string"
  description = "Here be dragons! In the future if this is set to Yes then automation will delete this resource on a schedule. Please set to No unless you know what you are doing"
  default     = "No"
}
