@F-103
Feature: F-103: New DefaultValue Column

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-103.1
  Scenario: Import Definition file with correctly configured DefaultValue in the CaseEventToFields tab
    Given a user with [an active profile in CCD]
    And a call [to import definition file] will get the expected response as in [Import_BEFTA_Master_Definition]
    When a request is prepared with appropriate values
    And it is submitted to call the [Fetch a Case Type Schema] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected
    And the response [contains the newly defined field - DefaultValue]

  @S-103.2
  Scenario: Import Definition file with blank values in the DefaultValue column of the CaseEventToFields tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a blank default value]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-103.3
  Scenario: Import Definition file without the DefaultValue column in the CaseEventToFields tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a missing default value column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected
