@F-091
Feature: F091: Upload definition with invalid category order (non-numeric) in the Category tab

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-391
  Scenario: must fail to Post Upload Document with invalid non-numeric category order
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Upload definition with invalid categories order] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
