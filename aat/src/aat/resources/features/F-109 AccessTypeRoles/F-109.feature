@F-109
Feature: F-109 Import validations for CCD Definition with AccessTypeRoles tab

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-109.2
  Scenario: Fail to import a definition file with invalid CaseTypeID in AccessTypeRoles
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessTypeRoles with invalid CaseTypeId]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-109.3
  Scenario: Fail to import a definition file invalid or duplicate AccessTypeID in AccessTypeRoles
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessTypeRoles with an invalid or duplicate AccessTypeID]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-109.4
  Scenario: Fail to import a definition file with missing OrganisationProfileID in AccessTypeRoles
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessTypeRoles missing OrganisationProfileID]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-109.5
  Scenario: Fail to import a definition file with missing Description in AccessTypeRoles when Display is set to True
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessTypeRoles with missing Description when Display is set to True]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-109.6
  Scenario: Fail to import a definition file with missing Hint in AccessTypeRoles when Display is set to True
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessTypeRoles with missing Hint when Display is set to True]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-109.7
  Scenario: Fail to import a definition file with missing DisplayOrder in AccessTypeRoles when Display is set to True
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessTypeRoles missing DisplayOrder when Display is set to True]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-109.8
  Scenario: Fail to import a definition file with DisplayOrder set to 0 in AccessTypeRoles when Display is set to True
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessTypeRoles with DisplayOrder set to 0 when Display is set to True]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-109.9
  Scenario: Fail to import a definition file with DisplayOrder not being unique across CaseTypeId for a Jurisdiction in AccessTypeRoles
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessTypeRoles where DisplayOrder is not unique across CaseTypeId for a Jurisdiction]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-109.10
  Scenario: Fail to import a definition file with OrganisationalRoleName and GroupRoleName both null in AccessTypeRoles
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessTypeRoles where both OrganisationalRoleName and GroupRoleName are null]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-109.11
  Scenario: Fail to import a definition file containing new Tab AccessTypeRoles with CaseAssignedRoleField is null in AccessTypeRoles
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessTypeRoles with CaseAssignedRoleField is null when GroupRoleName is not null]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-109.12
  Scenario: Fail to import a definition file containing new Tab AccessTypeRoles with GroupAccessEnabled is null  in AccessTypeRoles
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessTypeRoles with GroupAccessEnabled is null when GroupRoleName is not null]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-109.13
  Scenario: Fail to import a definition file with AccessMandatory set to an invalid value in AccessTypeRoles
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessTypeRoles with AccessMandatory set to an invalid value]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-109.14
  Scenario: Fail to import a definition file with AccessDefault set to an invalid value in AccessTypeRoles
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessTypeRoles with AccessDefault set to an invalid value]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-109.15
  Scenario: Fail to import a definition file with Display set to an invalid value in AccessTypeRoles
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessTypeRoles with Display set to an invalid value]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-109.16
  Scenario: Fail to import a definition file with GroupAccessEnabled set to an invalid value in AccessTypeRoles
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessTypeRoles with GroupAccessEnabled set to an invalid value]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-109.17
  Scenario: Fail to import a definition file with with AccessMandatory set to a valid value in AccessTypeRoles
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessTypeRoles with AccessMandatory set to a valid value]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-109.18
  Scenario: Fail to import a definition file with AccessDefault set to a valid value in AccessTypeRoles
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessTypeRoles with AccessDefault set to a valid value]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-109.22
  Scenario: Fail to import a definition file containing new Tab AccessTypeRoles with CaseAccessGroupIDTemplate has invalid format when value present
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessTypeRoles with CaseAccessGroupIDTemplate has value of the format that does not match <service>[:<id1>[:<Id2...]]]]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-109.23
  Scenario: Fail to import a definition file containing new Tab AccessTypeRoles with CaseAssignedRoleField has non matching value in RoleToAccessProfiles present
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessTypeRoles with CaseAssignedRoleField has value that does not match a value in RoleToAccessProfiles]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected


