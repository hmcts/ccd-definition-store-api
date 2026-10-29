@F-139
Feature: F-139: Create Draft (/api/draft)

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-139.1
  Scenario: Reject a draft request without a jurisdiction
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Create Draft Without Jurisdiction] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-139.2
  Scenario: Reject unauthenticated draft requests
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Create Draft Without Valid Authentication] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
