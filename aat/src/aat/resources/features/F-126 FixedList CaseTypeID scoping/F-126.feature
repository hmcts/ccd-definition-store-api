@F-126
Feature: F-126: FixedList CaseTypeID scoping on import (CCD-6594)

  Background:
    Given an appropriate test context as detailed in the test data source,

  @S-126.1
  Scenario: Import succeeds when the same FixedList ID is reused across two case types, each tagged with CaseTypeID
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition with the same FixedList ID across two case types, each tagged with CaseTypeID]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected
