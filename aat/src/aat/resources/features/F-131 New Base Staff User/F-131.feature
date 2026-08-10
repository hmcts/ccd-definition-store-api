@F-131
Feature: F-131: Test new base types

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-131.1
  Scenario: Import definition file with new base type StaffUser
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a definition file contains StaffUser base type]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected


  @S-131.4
  Scenario: A new case is created with StaffUser base type
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured event details]
    And the request [contains StaffUser base type with idamId]
    And it is submitted to call the [Get Case Type Details] operation of [CCD Definition Store]
    And the response [has the 200 OK code]
    Then a positive response is received
    And the response has all other details as expected


  @S-131.2
  Scenario: Fetch all base types returns StaffUser base type details
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Fetch All Base Types] operation of [CCD Definition Store]
    Then the response [has the 200 OK code]
    And the response [contains a list of all the base type data structures]
    And the response has all other details as expected
