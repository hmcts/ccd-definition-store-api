@F-092
Feature: F092: Upload definition with missing CategoryGroupId in the Category tab

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-392
  Scenario: must fail to Post Upload Document with missing CategoryGroupId
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Upload definition with missing CategoryGroupId in Categories tab] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
