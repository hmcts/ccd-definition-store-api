@F-091
Feature: F-091: 'ChangeOranisationRequest' Base Complex Type

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-091.1
  Scenario: Must successfully import a definition file containing the NoticeofChange config tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    And the response [contains valid noc config entries]
    Then a positive response is received
    And the response has all other details as expected

  @S-091.2
  Scenario: Must return a negative response in an attempt to import a definition file containing a NoticeofChange config tab with invalid case type
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains case type not defined]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received

  @S-091.3
  Scenario: Must return a negative response in an attempt to import a definition file containing a NoticeofChange config tab with multiple entries per case type
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains multiple noc config entries per config type]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received

  @S-091.4
  Scenario: Must return a negative response in an attempt to import a definition file containing a invalid values in NoticeofChange config tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains invalid noc config values]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received

