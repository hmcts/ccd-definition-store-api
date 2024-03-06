@F-111
Feature: F-111 Import validations for CCD Definition with AccessType tab

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-111.1
  Scenario: Fail to import a definition file with invalid CaseTypeID in AccessType
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessType with invalid CaseTypeId]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-111.3
  Scenario: Fail to import a definition file with missing OrganisationProfileID in AccessType
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessType missing OrganisationProfileID]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-111.4
  Scenario: Fail to import a definition file with missing Description in AccessType when Display is set to True
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessType with missing Description when Display is set to True]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-111.5
  Scenario: Fail to import a definition file with missing Hint in AccessType when Display is set to True
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessType with missing Hint when Display is set to True]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-111.6
  Scenario: Fail to import a definition file with missing DisplayOrder in AccessType when Display is set to True
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessType missing DisplayOrder when Display is set to True]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-111.7
  Scenario: Fail to import a definition file with DisplayOrder set to 0 in AccessType when Display is set to True
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessType with DisplayOrder set to 0 when Display is set to True]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-111.8
  Scenario: Fail to import a definition file with DisplayOrder not being unique across CaseTypeId for a Jurisdiction in AccessType
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessType where DisplayOrder is not unique across CaseTypeId for a Jurisdiction]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-111.9
  Scenario: Fail to import a definition file containing new Tab AccessType with a combination of CaseTypeId, AccessTypeId and OrganisationProfileId is not unique for a jurisdiction
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessType with a combination of CaseTypeId, AccessTypeId and OrganisationProfileId is not unique]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-111.10
  Scenario: Fail to import a definition file with AccessMandatory set to an invalid value in AccessType
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessType with AccessMandatory set to an invalid value]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-111.11
  Scenario: Fail to import a definition file with AccessDefault set to an invalid value in AccessType
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessType with AccessDefault set to an invalid value]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-111.12
  Scenario: Fail to import a definition file with Display set to an invalid value in AccessType
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab AccessType with Display set to an invalid value]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected




