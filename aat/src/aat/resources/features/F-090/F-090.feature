@F-090
Feature: F090: Upload definition with valid categories set in Category, CaseField, FixedList and ComplexTypes tabs

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-390
  Scenario: must return the case type for an appropriate request with valid categories
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [fetch a Case Type Schema] operation of [CCD Definition Store]
    Then the response [has the 200 OK code]
    And the response has all other details as expected
