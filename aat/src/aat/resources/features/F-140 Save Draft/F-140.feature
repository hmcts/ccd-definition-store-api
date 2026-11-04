@F-140
Feature: F-140: Save Draft (/api/draft/save)

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-140.1
  Scenario: Reject saving a draft without a description
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Save Draft Without Description] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-140.2
  Scenario: Reject saving a draft without an author
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Save Draft Without Author] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
