@F-127
Feature: F-127 Import validations for CCD Definition with CaseTypeTab ListElementCode

  Background:
    Given an appropriate test context as detailed in the test data source,

  @S-127.1
  @AC-1 @AC-API
  Scenario: Return direct CaseTypeTab ListElementCode value in tab structure API response
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a case type id with a direct CaseTypeTab ListElementCode value]
    And it is submitted to call the [Tab Structure By CaseType] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-127.2
  @AC-3 @AC-API
  Scenario: Return blank CaseTypeTab ListElementCode as whole field in tab structure API response
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a case type id with a blank CaseTypeTab ListElementCode value]
    And it is submitted to call the [Tab Structure By CaseType] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-127.3
  @AC-5 @AC-API
  Scenario: Return nested CaseTypeTab ListElementCode value in tab structure API response
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a case type id with a nested CaseTypeTab ListElementCode value]
    And it is submitted to call the [Tab Structure By CaseType] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-127.4
  @AC-API
  Scenario: Return multiple CaseTypeTab ListElementCode values in tab structure API response
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a case type id with multiple CaseTypeTab ListElementCode values]
    And it is submitted to call the [Tab Structure By CaseType] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-127.5
  @AC-2
  Scenario: Import definition file with invalid CaseTypeTab ListElementCode path
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a CaseTypeTab ListElementCode path that does not exist]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-127.6
  @AC-4
  Scenario: Import definition file with CaseTypeTab ListElementCode on a simple field
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a CaseTypeTab ListElementCode value on a simple field]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-127.7
  @AC-6
  Scenario: Import definition file with CaseTypeTab ListElementCode on a collection field
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a CaseTypeTab ListElementCode value on a collection field]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-127.8
  @AC-7
  Scenario: Import definition file with CaseTypeTab ListElementCode on a collection complex field
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a CaseTypeTab ListElementCode value on a collection complex field]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
