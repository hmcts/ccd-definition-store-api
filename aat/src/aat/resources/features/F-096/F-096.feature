@F-096
Feature: F-096: Import validations for the ChangeOrganisationRequest field and caseworker-caa/caseworker-approver events

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-096.1 #AC1
  Scenario: Must return a negative response in an attempt to import a definition file when field ChangeOrganisationRequest is defined twice within one casetype
    Given a user with [permissions to import case definition files]
    When a request is prepared with appropriate values
    And the request [contains field ChangeOrganisationRequest defined twice within one casetype]
    And it is submitted to call the [POST Case Definition File] operation of [CCD Definition Store Api]
    Then a negative response is received
    And the response has all other details as expected
    And the response [provides a clear validation message describing why the definition import has failed, for example, Change Organisation Request is defined more than once for case type 'CaseTypeID' in worksheet [TabName]]

  @S-096.2 #AC2
   Scenario: Must return a negative response in an attempt to import a definition file when there is more than on event per caseType for caseworker-caa
     Given a user with [permissions to import case definition files]
     When a request is prepared with appropriate values
     And the request [contains two events for a caseType with userRole caseworker-caa]
     And it is submitted to call the [POST Case Definition File] operation of [CCD Definition Store Api]
     Then a negative response is received
     And the response has all other details as expected
     And the response [provides a clear validation message describing why the definition import has failed, for example, UserRole 'caseworker-caa' is defined more than once for case type 'CaseTypeID' in worksheet [TabName]]


