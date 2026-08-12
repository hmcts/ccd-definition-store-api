@F-129
Feature: F-129: Test new base types Summary, Roles, Hearings and Task

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-129.1
  Scenario: Import Definition File with valid base types: Summary base type
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with newly added Summary base type in CaseTypeTab with required display order]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected
    And a call [get tabs for case type id which has summary tab] will get the expected response as in [S-129.1_Get_Tab_Structure]

  @S-129.2
  Scenario: Return the new Summary base type in the Get Case Type Definition Store operation
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains id of a case type with Summary base type fields]
    And it is submitted to call the [Get Case Type Details] operation of [CCD Definition Store]
    Then a positive response is received
    And the response [contains all details of the case type requested]
    And the response has all other details as expected
