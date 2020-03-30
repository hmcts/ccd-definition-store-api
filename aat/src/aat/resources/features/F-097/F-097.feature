@F-097
Feature: F097: Upload definition with invalid categoryIds in ComplexTypes

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-397
  Scenario: must fail to Post Upload Document with a category on non Document type
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Upload definition with invalid categoryIds in ComplexTypes] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
