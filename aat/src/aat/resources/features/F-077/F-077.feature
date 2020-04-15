@F-077
Feature: F-077: Fetch All Base Types
  Background:
    Given an appropriate test context as detailed in the test data source

  @S-331
  Scenario: Should return all valid base types
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Fetch All Base Types] operation of [CCD Definition Store]
    And the response [has the 200 OK code]
    And the response [contains a list of all the base type data structures]
    And the response has all other details as expected

  @S-329 @Ignore # Response code mismatch, expected: 401, actual: 403 RDM-6628
  Scenario: must return 401 when request does not provide valid authentication credentials
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains an invalid authentication credentials]
    And it is submitted to call the [Fetch All Base Types] operation of [CCD Definition Store]
    Then a negative response is received
    And the response [contains 401 unauthorised code]
    And the response has all the details as expected

  @S-330 @Ignore # Response code mismatch, expected: 403, actual: 200 Is this valid?
  Scenario: must return 403 when request provides authentic credentials without authorised access to the operation
    Given a user with [an active profile in CCD, and insufficient privilege to the operation]
    When a request is prepared with appropriate values
    And it is submitted to call the [Fetch All Base Types] operation of [CCD Definition Store]
    Then a negative response is received
    And the response [contains 403 forbidden code]
    And the response has all the details as expected
