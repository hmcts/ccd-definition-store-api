@F-133
Feature: F-133: Get Jurisdiction UI Configs (/api/display/jurisdiction-ui-configs)

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-133.1
  Scenario: Return jurisdiction UI configs
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get Jurisdiction UI Configs] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-133.2
  Scenario: Return empty jurisdiction UI configs when jurisdictions are omitted
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get Jurisdiction UI Configs Without Jurisdictions] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected
