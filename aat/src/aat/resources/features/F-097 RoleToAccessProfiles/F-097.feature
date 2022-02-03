@F-097
Feature: F-097: Role To Access Profiles

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-097.1
  Scenario: Must return a negative response in an attempt to import a definition file containing invalid case type id in RoleToAccessProfiles
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains role to access profiles with invalid case type id]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-097.2
  Scenario: Must return a negative response in an attempt to import a definition file containing invalid access profile in RoleToAccessProfiles
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains invalid access profile]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-097.3
  Scenario: Must return a negative response in an attempt to import a definition file containing invalid readonly value in RoleToAccessProfiles
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains invalid readonly value]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-097.4
  Scenario: Must return a negative response in an attempt to import a definition file containing invalid disabled value in RoleToAccessProfiles
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains invalid disabled value]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-097.5
  Scenario: Must successfully import a definition file containing  a valid Role to access profiles
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a valid role to access profiles]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-097.6
  Scenario: Import Definition file with blank values in the CaseAccessCategories column of the RoleToAccessProfiles tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a blank CaseAccessCategories value]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-097.7
  Scenario: Import Definition file without the CaseAccessCategories column in the RoleToAccessProfiles tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a missing CaseAccessCategories column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-097.8
  Scenario: Return the new CaseAccessCategories column as part of the Get Case Type operation from Definitions store
    Given a user with [an active profile in CCD]
    And a call [to import definition file] will get the expected response as in [Import_BEFTA_Master_Definition]

    When a request is prepared with appropriate values
    And it is submitted to call the [Fetch a Case Type Schema] operation of [CCD Definition Store]

    Then a positive response is received
    And the response has all other details as expected
    And the response [contains the newly defined field - CaseAccessCategories]
