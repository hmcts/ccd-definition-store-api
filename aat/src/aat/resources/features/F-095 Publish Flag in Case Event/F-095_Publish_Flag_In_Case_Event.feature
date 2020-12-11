@F-095
Feature: F-095: Publish Events column

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-095.1  # ACA-1
  Scenario: Must successfully import a definition file containing a valid value in Publish Events column
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a valid value configured in Publish Events column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected
    And a call [fetch a Case Type Schema] will get the expected response as in [S-095.1_Get_CaseType].

  @S-095.2 # ACA-2
  Scenario: Must return a negative response in an attempt to import a definition file containing a invalid value Publish Events column
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a invalid value configured in Publish Events column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-095.3  # ACA-3
  Scenario: Must successfully import a definition file containing a blank value in Publish Events column
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a blank value in Publish Events column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected
    And a call [fetch a Case Type Schema] will get the expected response as in [S-095.3_Get_CaseType].


