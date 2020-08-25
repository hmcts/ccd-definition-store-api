@F-089
Feature: F-089: 'Organisation' and 'OrganisationPolicy' Base Complex Types

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-089.1 @Ignore # ES existing index has the old type for dynamic lists - reenable after 19.1.1 Release
  Scenario: must successfully import a definition file containing some Organisation and OrganisationPolicy fields
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured DefaultValue in EventToComplexTypes tab]
    And it is submitted to call the [POST Case Definition File] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-089.2
  Scenario: must return all details successfully for a case type containing some Organisation and OrganisationPolicy fields
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a valid jurisdiction id]
    And the request [contains id of a case type with some Organisation and OrganisationPolicy fields]
    And it is submitted to call the [Get Case Type Details] operation of [CCD Definition Store]
    Then a positive response is received
    And the response [contains all details of the case type requested]
    And the response has all other details as expected

  @S-089.3
  Scenario: must return a negative response in an attempt to import a definition file containing some Organisation and OrganisationPolicy fields with invalid data
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains invalid default value]
    And it is submitted to call the [POST Case Definition File] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

