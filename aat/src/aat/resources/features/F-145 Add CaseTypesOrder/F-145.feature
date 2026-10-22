@F-145
Feature: F-145: Filter jurisdiction case types by CaseTypesOrder

  # CCD-7772: the agreed behaviour filters by caseTypeOrder and sorts matches by ID.
  # With no filter the response has no specified case-type order.
  Background:
    Given an appropriate test context as detailed in the test data source

  @S-145.1
  Scenario: AC01: filter by CaseTypesOrder and sort matching case types by ID
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a valid jurisdiction id]
    And the request [contains a caseTypeOrder filter]
    And it is submitted to call the [Get Jurisdiction Details] operation of [CCD Definition Store]
    Then the response [has the 200 OK code]
    And the response [contains the expected case types and optional caseTypesOrder values]
    And the response has all other details as expected

  @S-145.2
  Scenario: AC02: return legacy case types without requiring an order
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a valid jurisdiction id]
    And it is submitted to call the [Get Jurisdiction Details] operation of [CCD Definition Store]
    Then the response [has the 200 OK code]
    And the response [contains the expected case types and optional caseTypesOrder values]
    And the response has all other details as expected

  @S-145.3
  Scenario: return numbered and unnumbered case types when the filter is absent
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a valid jurisdiction id]
    And it is submitted to call the [Get Jurisdiction Details] operation of [CCD Definition Store]
    Then the response [has the 200 OK code]
    And the response [contains the expected case types and optional caseTypesOrder values]
    And the response has all other details as expected

  @S-145.4
  Scenario: accept the minimum configured order value of 1
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a valid jurisdiction id]
    And the request [contains a caseTypeOrder filter]
    And it is submitted to call the [Get Jurisdiction Details] operation of [CCD Definition Store]
    Then the response [has the 200 OK code]
    And the response [contains the expected case types and optional caseTypesOrder values]
    And the response has all other details as expected

  @S-145.5
  Scenario: return the maximum configured order value of 99
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a valid jurisdiction id]
    And the request [contains a caseTypeOrder filter]
    And it is submitted to call the [Get Jurisdiction Details] operation of [CCD Definition Store]
    Then the response [has the 200 OK code]
    And the response [contains the expected case types and optional caseTypesOrder values]
    And the response has all other details as expected

  @S-145.6
  Scenario: retain the jurisdiction with no case types when the filter has no matches
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a valid jurisdiction id]
    And the request [contains a caseTypeOrder filter]
    And it is submitted to call the [Get Jurisdiction Details] operation of [CCD Definition Store]
    Then the response [has the 200 OK code]
    And the response [contains the expected case types and optional caseTypesOrder values]
    And the response has all other details as expected
