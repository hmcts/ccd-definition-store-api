@F-126
Feature: F-126: Reindexing functionality and verification via GET API

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-126.1
  Scenario: Successfully trigger reindexing
    Given a user with [an active profile in CCD and with CCD-import role]
    When a request is prepared with appropriate values
    And the request [contains reindexing set to true]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received

  @S-126.2
  Scenario: Successfully retrieve reindexing metadata for all case types
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get all reindexing tasks] operation of [CCD Definition Store]
    Then a positive response is received
    Then the response [has the 200 OK code]
    And the response [contains reindexing metadata for all case types]

  @S-126.3
  Scenario: Successfully retrieve reindexing metadata for provided case type
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [includes a caseType query parameter with value "CaseTypeA"]
    And it is submitted to call the [Get filtered reindexing tasks] operation of [CCD Definition Store]
    Then a positive response is received
    Then the response [has the 200 OK code]
    And the response [contains tasks only related to CaseTypeA]

    @S-126.4
    Scenario: Retrieve no reindexing metadata when no reindexing tasks exist
      Given a user with [an active profile in CCD]
      When a request is prepared with appropriate values
      And it is submitted to call the [Get all reindexing tasks] operation of [CCD Definition Store]
      Then a positive response is received
      Then the response [has the 200 OK code]
      And the response [contains an empty list of reindexing tasks]

    @S-126.5
    Scenario: Retrieve no reindexing metadata for non-existing case type
      Given a user with [an active profile in CCD]
      When a request is prepared with appropriate values
      And the request [includes a caseType query parameter with value "NonExistingCaseType"]
      And it is submitted to call the [Get filtered reindexing tasks] operation of [CCD Definition Store]
      Then a positive response is received
      Then the response [has the 200 OK code]
      And the response [contains an empty list of reindexing tasks]