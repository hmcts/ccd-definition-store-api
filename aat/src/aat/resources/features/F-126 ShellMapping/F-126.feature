@F-126
Feature: F-126 Import validations for CCD Definition with ShellMapping tab

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-126.1
  Scenario: Import a definition file containing new Tab ShellCaseMapping - successful import
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab ShellMapping]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-126.2
  Scenario: Import a definition file containing new Tab ShellCaseMapping with invalid ShellCaseTypeID - not successful import
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab ShellCaseMapping with invalid ShellCaseTypeId]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-126.3
  Scenario: Import a definition file containing new Tab ShellCaseMapping with invalid OriginatingCaseTypeID - not successful import
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab ShellCaseMapping with invalid OriginatingCaseTypeId]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-126.4
  Scenario: Import a definition file containing new Tab ShellCaseMapping with invalid ShellCaseFieldName - not successful import
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab ShellCaseMapping with invalid ShellCaseFieldName(The field does not belong to the ShellCaseTypeID)]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-126.5
  Scenario: Import a definition file containing new Tab ShellCaseMapping with invalid OriginatingCaseFieldName - not successful import
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab ShellCaseMapping with invalid OriginatingCaseFieldName(The field does not belong to the OriginatingCaseTypeID)]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-126.6
  Scenario: Import a definition file containing new Tab ShellCaseMapping with duplicate caseTypeId - not successful import
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new tab ShellCaseMapping with duplicate CaseTypeID]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
