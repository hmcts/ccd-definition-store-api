@F-090
Feature: F-090: 'ChangeOranisationRequest' Base Complex Type

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-090.1
  Scenario: Must return valid base types: ChangeOrganisationRequest complex base type
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Fetch All Base Types] operation of [CCD Definition Store]
    And the response [has the 200 OK code]
    And the response [contains details pertaining to ChangeOrganisationRequest base type]
    Then a positive response is received
    And the response has all other details as expected

  @S-090.2
  Scenario: Must successfully import a definition file contain ChangeOrganisationRequest fields
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured ChangeOrganisationRequest Fields]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-090.3
  Scenario: Must return all details successfully for a case type containing ChangeOrganisationRequest fields
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains id of a case type with ChangeOrganisationRequest fields]
    And it  is submitted to call the [Get Case Type Details] operation of [CCD Definition Store]
    Then a positive response is received
    And the response [contains all details of the case type requested]
    And the response has all other details as expected

