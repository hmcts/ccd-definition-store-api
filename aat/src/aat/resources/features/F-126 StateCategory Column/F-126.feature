@F-126
Feature: F-126: New StateCategory Column

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-126.1
  Scenario: Import a definition file containing new column StateCategory with single StateCategory
    Given a user with [an active profile in CCD and with CCD-import role]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new column StateCategory with single StateCategory]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-126.2
  Scenario: Import a definition file containing new column StateCategory with comma separated State Categories
    Given a user with [an active profile in CCD and with CCD-import role]
    When a request is prepared with appropriate values
    And the request [contains a definition file with new column StateCategory with comma separated State Categories]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-126.3
  Scenario: Get Case Type Details should include stateCategory for single StateCategory import
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a valid jurisdiction id]
    And the request [contains a valid caseType id]
    And it is submitted to call the [Get Case Type Details] operation of [CCD Definition Store]
    Then the response [has the 200 OK code]
    And the response [contains case type details with stateCategory]

  @S-126.4
  Scenario: Get Case Type Details should include stateCategory for comma separated State Categories import
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a valid jurisdiction id]
    And the request [contains a valid caseType id for comma separated state categories]
    And it is submitted to call the [Get Case Type Details] operation of [CCD Definition Store]
    Then the response [has the 200 OK code]
    And the response [contains case type details with stateCategory]
