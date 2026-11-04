@F-137
Feature: F-137: Get Import Audits (/api/import-audits)

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-137.1
  Scenario: Return import audits
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get Import Audits] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-137.2
  Scenario: Reject unauthenticated import-audit requests
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get Import Audits Without Valid Authentication] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
