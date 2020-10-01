@F-092
Feature: F-092: Conditional Event Post State

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-092.1 @Ignore
  Scenario: Must return a negative response in an attempt to import a definition file containing conditional event post state with invalid case field
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains conditional event post state with invalid case field]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received

  @S-092.2 @Ignore
  Scenario: Must return a negative response in an attempt to import a definition file containing conditional event post state with duplicate priority
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains conditional event post state with duplicate priority]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received

  @S-092.3 @Ignore
  Scenario: Must return a negative response in an attempt to import a definition file containing conditional event post state with no default
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains conditional event post state with no default post state]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received

  @S-092.4
  Scenario: Must return a negative response in an attempt to import a definition file containing conditional event post state with missing priority
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains conditional event post state with missing priority]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-092.5
  Scenario: Must return a negative response in an attempt to import a definition file containing conditional event post state with invalid condition
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains conditional event post state with invalid condition]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-092.6
  Scenario: Must return a negative response in an attempt to import a definition file containing conditional event post state with invalid state
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains conditional event post state with invalid state]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

