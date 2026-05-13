@F-126
Feature: F-126: New DefaultFocus Column

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-126.1
  Scenario: Import Definition file with true value in DefaultFocus column of the CaseTypeTab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new column DefaultFocus]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-126.2
  Scenario: Import a definition file containing new column DefaultFocus and having more than one true DefaultFocus
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains DefaultFocus set to true for more than one tab for same CaseType]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received


