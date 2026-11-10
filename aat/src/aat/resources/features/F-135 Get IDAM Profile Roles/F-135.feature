@F-135
Feature: F-135: Get IDAM Profile Roles (/api/idam/profile/roles)

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-135.1
  Scenario: Return the IDAM profile roles
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get IDAM Profile Roles] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected
