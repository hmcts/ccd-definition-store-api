@F-132
Feature: F-132: Get Banners (/api/display/banners)

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-132.1
  Scenario: Return banners for a jurisdiction
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get Banners] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-132.2
  Scenario: Return empty banners when jurisdictions are omitted
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get Banners Without Jurisdictions] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-132.3
  Scenario: Reject unauthenticated display requests
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get Banners Without Valid Authentication] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
