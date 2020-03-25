@F-090 @focus
Feature: F090: Upload definition with invalid categories

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-390
  Scenario: must fail to Post Upload Document with invalid categories
    Given a user with [an active profile in CCD]
    When  a request is prepared with appropriate values
    And   it is submitted to call the [Upload definition with invalid categories] operation of [CCD Definition Store]
    Then  a positive response is received
    And   the response has all other details as expected
