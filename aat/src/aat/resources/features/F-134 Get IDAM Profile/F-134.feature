@F-134
Feature: F-134: Get IDAM Profile (/api/idam/profile)

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-134.1
  Scenario: Return the IDAM profile
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get IDAM Profile] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-134.2
  Scenario: Reject unauthenticated IDAM requests
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get IDAM Profile Without Valid Authentication] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
