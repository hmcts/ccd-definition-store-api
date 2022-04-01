@F-105
Feature: F-105: TTL Increment Events column

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-105.1 #AC-1
  Scenario: Must successfully import a definition file containing a valid value in TTL Increment Events column
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a valid value configured in TTL Increment Events column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected
    And a call [fetch a Case Type Schema] will get the expected response as in [S-105.1_Get_CaseType].

  @S-105.2 #AC-2
  Scenario: Must return a negative response in an attempt to import a definition file containing a invalid value TTLIncrement Events column
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a invalid value configured in TTLIncrement Events column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-105.3  #AC-3
  Scenario: Must successfully import a definition file containing a blank value in TTLIncrement Events column
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a blank value in TTL Increment Events column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected
    And a call [fetch a Case Type Schema] will get the expected response as in [S-105.3_Get_CaseType].

  @S-105.4 #AC-4
  Scenario: Must successfully import a definition file where TTLIncrement column is missing
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains missing TTLIncrement column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected
    And a call [fetch a Case Type Schema] will get the expected response as in [S-105.4_Get_CaseType].
