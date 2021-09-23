@F-099
Feature: F-099: 'TTL' Base Complex Type

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-099.1
  Scenario: Must successfully import a definition file that contains TTL fields
    Given a user with [an active profile in CCD],
    When a request is prepared with appropriate values,
    And the request [contains correctly configured TTL Fields],
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store],
    Then a positive response is received,
    And the response has all other details as expected.

  @S-099.2
  Scenario: Must return all details successfully for a case type containing TTL fields
    Given a user with [an active profile in CCD],
    When a request is prepared with appropriate values,
    And the request [contains id of a case type with TTl fields],
    And it  is submitted to call the [Get Case Type Details] operation of [CCD Definition Store],
    Then a positive response is received,
    And the response [contains all details of the case type requested],
    And the response has all other details as expected.

