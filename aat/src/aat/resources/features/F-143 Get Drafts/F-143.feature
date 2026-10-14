@F-143
Feature: F-143: Get Drafts (/api/drafts)

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-143.1
  Scenario: Return an empty list when no drafts exist for a jurisdiction
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get Drafts] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected
