@F-081
Feature: CCD Definition Store Api :: GET /api/data/jurisdictions

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-348 @Ignore # Response code mismatch, expected: 401, actual: 403 RDM-6628
  Scenario: must return 401 when request does not provide valid authentication credentials
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains an invalid authentication credentials]
    And it is submitted to call the [get jurisdiction details] operation of [CCD Definition Store]
    Then a negative response is received
    And the response [contains 401 unauthorised code]
    And the response has all the details as expected

  @S-349 @Ignore # Response code mismatch, expected: 403, actual: 200 Is this valid?
  Scenario: must return 403 when request provides authentic credentials without authorised access to the operation
    Given a user with [an active profile in CCD, and insufficient privilege to the case type]
    When a request is prepared with appropriate values
    And it is submitted to call the [get jurisdiction details] operation of [CCD Definition Store]
    Then a negative response is received
    And the response [contains 403 forbidden code]
    And the response has all the details as expected

  @S-347 #RDM-6858
  Scenario: must return 200 with List of jurisdictions
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a valid jurisdiction id]
    And it is submitted to call the [get jurisdiction details] operation of [CCD Definition Store]
    And the response [has the 200 OK code]
    And the response has all other details as expected

  @S-350 @Ignore # Response code mismatch, expected: 404, actual: 200 #RDM-7615
  Scenario: must return 404 when user provide non-existing JID {jurisdiction references} within the request
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a non-existing jurisdiction id]
    And it is submitted to call the [get jurisdiction details] operation of [CCD Definition Store]
    Then a negative response is received
    And the response [contains 404 not found code]
    And the response has all the details as expected
