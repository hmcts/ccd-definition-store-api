@F-000 @Smoke
Feature: [SAMPLE] Get health endpoint check

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-000
  Scenario: [SAMPLE] must return a successful response from the health endpoint
    When a request is prepared with appropriate values
    And it is submitted to call the [Health Endpoint] operation of [CCD Definition Store]
    Then a positive response is received
    And the response [has the 200 OK code]
    And the response has all other details as expected.
