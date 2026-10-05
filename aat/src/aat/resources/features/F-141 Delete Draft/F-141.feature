@F-141
Feature: F-141: Delete Draft (/api/draft/{jurisdiction}/{version})

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-141.1
  Scenario: Reject deletion of an unknown draft version
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Delete Unknown Draft Version] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
