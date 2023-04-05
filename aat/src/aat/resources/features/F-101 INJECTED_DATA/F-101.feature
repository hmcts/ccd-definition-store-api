@F-101
Feature: F-101: Injected Data

  Background:
    Given an appropriate test context as detailed in the test data source


  @S-101.1
  Scenario: Import Definition file with [INJECTED_DATA.<value>] in FieldShowCondition TabShowCondition columns of the CaseTypeTab, ComplexTypes
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured CaseType tab with a field in the FieldShowCondition column, value starting with INJECTED_DATA]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-101.2
  Scenario: Import Definition file with [INJECTED_DATA.<value>] in EventEnablingCondition columns of the CaseEvent tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured CaseEvent tab with a field in the EventEnablingCondition column, value starting with INJECTED_DATA]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-101.5
  Scenario: Import Definition file with [INJECTED_DATA.<value>] in FieldShowCondition column of the CaseEventToFields tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured CaseEventToFields tab with a field in the FieldShowCondition column, value starting with INJECTED_DATA]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    And the response [has the 422 Bad Request code]
    Then a negative response is received
    And the response has all other details as expected

  @S-101.6 @Ignore # Response code mismatch, expected: 422, actual: 400 CCD-4469
  Scenario: Import Definition file with [INJECTED_DATA.<value>] in PageShowCondition column of the CaseEventToFields tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured CaseEventToFields tab with a field in the PageShowCondition column, value starting with INJECTED_DATA]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    And the response [has the 422 Bad Request code]
    Then a negative response is received
    And the response has all other details as expected


