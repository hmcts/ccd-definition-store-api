@F-136
Feature: F-136: Get Admin Web Authorisation (/api/idam/adminweb/authorization)

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-136.1
  Scenario: Return admin web authorisation
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get Admin Web Authorisation] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected
