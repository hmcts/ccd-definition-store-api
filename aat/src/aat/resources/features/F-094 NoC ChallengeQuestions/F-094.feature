@F-094
Feature: F-094 Retrieve ChallengeQuestions for Notice of Change

  Background:
    Given an appropriate test context as detailed in the test data source,

  @S-094.1
  Scenario: Must successfully return the contents of the ChallengeQuestion definition
    Given a user [MCA System - with an IDAM role of caseworker CAA],
    And the request [intends to get the contents of a set of questions],
    And the request [contains a valid CaseType ID],
    And the request [contains a valid Collection Question ID],
    And a request is prepared with appropriate values,
    When it is submitted to call the [GET ChallengeQuestion] operation of [CCD Definition Store Api],
    Then a positive response is received,
    And the response has all the details as expected.

  @S-094.2
  Scenario: must return an error response (empty list returned) for a malformed CaseType
    Given a user [MCA System - with an IDAM role of caseworker CAA],
    And a request is prepared with appropriate values,
    And the request [intends to get the contents of a set of questions],
    And the request [contains a malformed CaseType ID for C1],
    And the request [contains a valid Collection Question ID],
    When it is submitted to call the [GET ChallengeQuestion] operation of [CCD Definition Store Api],
    Then a positive response is received,
    And the response [returns an empty questions array]
    And the response has all the details as expected.

  @S-094.4
  Scenario: must return an error response (empty list returned) for an invalid Collection Question ID
    Given a user [MCA System - with an IDAM role of caseworker CAA],
    And a request is prepared with appropriate values,
    And the request [intends to get the contents of a set of questions],
    And the request [contains a valid CaseType ID for C1],
    And the request [contains a Collection Question ID that does not correspond to a tab containing a valid CaseTypeId],
    When it is submitted to call the [GET ChallengeQuestion] operation of [CCD Definition Store Api],
    Then a positive response is received,
    And the response [returns an empty questions array]
    And the response has all the details as expected.

  @S-094.5
  Scenario: Must return a negative response in an attempt to import a definition file with duplicate display order
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains challenge questions with duplicate display order]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-094.6
  Scenario: Must return a negative response in an attempt to import a definition file with duplicate question id
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains challenge questions with duplicate question id]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-094.7
  Scenario: Must return a negative response in an attempt to import a definition file with missing question group id
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains challenge questions with missing question group id]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
