@F-095
Feature: F-095: Event Enabling condition

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-095.1.EEC
  Scenario: Must return a negative response in an attempt to import a definition file containing conditional event post state with invalid case field
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains invalid event enabling condition]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-095.2.EEC
  Scenario: Must successfully import a definition file containing  a valid event enabling condition
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a valid event enabling condition]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected


