@F-107
Feature: F-107 CaseLink

  Background:
    Given an appropriate test context as detailed in the test data source,

  @S-107.1
  Scenario: Import Definition File with valid LinkReason and CaseLink base type
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains CaseLink base type]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected
    And a call [fetch Master Case Type to verify CaseLink and LinkReason CaseField base types] will get the expected response as in [S-107.1_Get_CaseType].

