@F-102
Feature: F-102: 'CQC' Base Complex Type

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-102.1
  Scenario: Must successfully import a definition file that contains CQC fields
    Given a user with [an active profile in CCD],
    When a request is prepared with appropriate values,
    And the request [contains correctly configured CQC Fields],
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store],
    Then a positive response is received,
    And the response has all other details as expected.

  @S-102.2
  Scenario: Must return all details successfully for a case type containing CQC fields
    Given a user with [an active profile in CCD],
    When a request is prepared with appropriate values,
    And the request [contains id of a case type with CQC fields],
    And it is submitted to call the [Fetch a Case Type Schema] operation of [CCD Definition Store],
    Then a positive response is received,
    And the response [contains all details of the case type requested],
    And the response has all other details as expected.
