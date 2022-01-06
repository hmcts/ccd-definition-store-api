@F-100
Feature: F-100: New DefaultValue Column

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-100.1
  Scenario: Import Definition file with correctly configured DefaultValue in the CaseEventToFields tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a valid default value]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-100.2
  Scenario: Import Definition file with blank values in the DefaultValue column of the CaseEventToFields tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a blank default value]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-100.3
  Scenario: Import Definition file without the DefaultValue column in the CaseEventToFields tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a missing default value column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected
