@F-094
Feature: F094: Upload definition with a category on non Document type in ComplexTypes tab

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-394
  Scenario: must fail to Post Upload Document with a category on non Document type
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Upload definition with a category in ComplexTypes on non Document type] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
