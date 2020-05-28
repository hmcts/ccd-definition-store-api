@F-089
Feature: F-089: Get Case Type with Organisation and OrganisationPolicy fields

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-089.1
  Scenario: must return case type details for the request
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a valid jurisdiction id]
    And the request [contains id of a case type with Organisation and OrganisationPolicy fields]
    And it is submitted to call the [Get Case Type Details] operation of [CCD Definition Store]
    And the response [has the 200 OK code]
    And the response [contains case type details]
    And the response has all other details as expected

