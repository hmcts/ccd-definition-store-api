@F-104
Feature: F-104 Categories

  Background:
    Given an appropriate test context as detailed in the test data source,

  @S-104.1
  Scenario: Must return a negative response in an attempt to import a definition file with invalid display order in sub categories
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains categories with invalid display order in sub categories]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-104.2
  Scenario: Must return a negative response in an attempt to import a definition file with invalid CaseTypeId
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains categories with invalid CaseTypeId]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-104.3
  Scenario: Must return a negative response in an attempt to import a definition file with null CaseTypeId
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains categories with null CaseTypeId]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-104.4
  Scenario: Must return a negative response in an attempt to import a definition file with null CategoryID
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains categories with null CategoryID]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-104.5
  Scenario: Must return a negative response in an attempt to import a definition file with duplicate display order
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains categories with duplicate display order]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-104.6
  Scenario: Must return a negative response in an attempt to import a definition file with negative display order
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains categories with negative display order]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-104.7
  Scenario: Must return a negative response in an attempt to import a definition file with an invalid ParentCategoryID
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains categories with incorrect ParentCategoryID]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-104.8
  Scenario: Must return a negative response in an attempt to import a definition file with null CategoryLabel
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains categories with null CategoryLabel]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

