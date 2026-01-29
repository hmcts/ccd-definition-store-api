@F-127
Feature: F-127 Get Shell Mappings for Originating Case Type Id

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-127.2
  Scenario: Success response - Return 200 success with shell case mappings for the originalCaseTypeId
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains an originalCaseTypeId that exists in CCD config]
    And it is submitted to call the [Get Shell Case Type Details] operation of [CCD Definition Store]
    Then a positive response is sent back
    And the response [contains all shellCaseMapping for originalCaseTypeId is present in the response]

  @S-127.3
  Scenario: Negative response - Return 404 if no shell case mappings are present for the originalCaseTypeId
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains an originalCaseTypeId that exists in CCD config]
    And there are no shellCaseMappings for the originalCaseTypeId
    And it is submitted to call the [Get Shell Case Type Details] operation of [CCD Definition Store]
    Then a negative response is received
    And the response [contains 404 Not Found - No ShellCaseMapping Found]

  @S-127.4
  Scenario: Negative response - Return 400 if originalCaseTypeId pass is not valid
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains an originalCaseTypeId that does not exists in CCD config]
    And it is submitted to call the [Get Shell Case Type Details] operation of [CCD Definition Store]
    Then a negative response is received
    And the response [contains 400 Bad Request - invalid case type id]

  @S-127.5
  Scenario: Negative response - Return 400 Bad Request
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured values]
    And the request does not have originalCaseTypeId
    And it is submitted to call the [Get Shell Case Type Details] operation of [CCD Definition Store]
    Then a negative response is received
    And the response [contains 400 Bad Request]

  @S-127.6
  Scenario: Negative response - Return 401 Unauthorised
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured values]
    And the request [contains an invalid S2S token]
    And it is submitted to call the [Get Shell Case Type Details] operation of [CCD Definition Store]
    Then a negative response is received
    And the response [contains 401 Unauthorised]
