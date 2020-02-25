@F-078
Feature: F-078: Get Version of Case Type

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-335
  Scenario: must return 200 and Gets the current version of a Case Type Schema
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a valid case type id]
    And it is submitted to call the [Gets The Current Version Of A Case Type Schema] operation of [CCD Definition Store]
    And the response [has the 200 OK code]
    And the response [returns the version number for case type id BEFTA_CASETYPE_2_1]
    And the response has all the details as expected

  @S-332 @Ignore
  Scenario: more tests

  @S-333 @Ignore
  Scenario: Case type version information (Green path /Not found)

  @S-334 @Ignore
  Scenario: Case type version information (Green path /Not found)

  @S-336 @Ignore # Response code mismatch, expected: 401, actual: 403 RDM-6628
  Scenario: must return 401 when request does not provide valid authentication credentials
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains an invalid authentication credentials]
    And it is submitted to call the [Gets The Current Version Of A Case Type Schema] operation of [CCD Definition Store]
    Then a negative response is received
    And the response [contains 401 unauthorised code]
    And the response has all the details as expected

  @S-337 @Ignore # Response code mismatch, expected: 403, actual: 200
  Scenario: must return 403 when request provides authentic credentials without authorised access to the operation
    Given a user with [an active profile in CCD, and insufficient privilege to the case type]
    When a request is prepared with appropriate values
    And it is submitted to call the [Gets The Current Version Of A Case Type Schema] operation of [CCD Definition Store]
    Then a negative response is received
    And the response [contains 403 forbidden code]
    And the response has all the details as expected

  @S-338
  Scenario: must return 404 when user provide non-existing Case Type ID within the request
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains non existing case type id]
    And it is submitted to call the [Gets The Current Version Of A Case Type Schema] operation of [CCD Definition Store]
    Then a negative response is received
    And the response [contains 404 not found]
    And the response has all the details as expected
