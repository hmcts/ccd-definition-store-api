@F-109
Feature: F-109 Import validations for CCD Definition with AccessTypeRole tab

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-109.1
  Scenario: Fail to import a definition file containing new Tab AccessTypeRole with a combination of CaseTypeId, AccessTypeId and OrganisationProfileId is not present in new Tab AccessType
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessTypeRole with a combination of CaseTypeId, AccessTypeId and OrganisationProfileId is not present in new Tab AccessType]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-109.2
  Scenario: Fail to import a definition file with OrganisationalRoleName and GroupRoleName both null in AccessTypeRole
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessTypeRole where both OrganisationalRoleName and GroupRoleName are null]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-109.3
  Scenario: Fail to import a definition file containing new Tab AccessTypeRole with CaseAssignedRoleField is null in AccessTypeRole
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessTypeRole with CaseAssignedRoleField is null when GroupRoleName is not null]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-109.4
  Scenario: Fail to import a definition file containing new Tab AccessTypeRole with GroupAccessEnabled is null in AccessTypeRole
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessTypeRole with GroupAccessEnabled is null when GroupRoleName is not null]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-109.5
  Scenario: Fail to import a definition file with GroupAccessEnabled set to an invalid value in AccessTypeRole
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessTypeRole with GroupAccessEnabled set to an invalid value]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-109.6
  Scenario: Fail to import a definition file containing new Tab AccessTypeRole with CaseAccessGroupIDTemplate has invalid format when value present
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessTypeRole with CaseAccessGroupIDTemplate has value of the format that does not match <service>[:<id1>[:<Id2...]]]]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-109.7
  Scenario: Fail to import a definition file containing new Tab AccessTypeRole with CaseAssignedRoleField has non matching value in RoleToAccessProfiles present
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessTypeRole with CaseAssignedRoleField has value that does not match a value in RoleToAccessProfiles]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected


