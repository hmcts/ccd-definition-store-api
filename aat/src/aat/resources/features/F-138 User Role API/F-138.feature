@F-138
Feature: F-138: User Role API (/api/user-role)

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-138.1
  Scenario: Retrieve an existing user role
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get User Role] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-138.2
  Scenario: Return not found for an unknown user role
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get Unknown User Role] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-138.3
  Scenario: Reject an invalid Base64 user role query parameter
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get User Role With Invalid Base64] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-138.4
  Scenario: Reject a missing user role query parameter
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get User Role Without Role] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-138.5
  Scenario: Reject unauthenticated user-role requests
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get User Role Without Valid Authentication] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-138.6
  Scenario: Reject creation of a duplicate user role
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Create Duplicate User Role] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-138.7
  Scenario: Reject a user role without a role value
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Create User Role Without Role] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-138.8
  Scenario: Reject a user role without a security classification
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Create User Role Without Security Classification] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-138.9
  Scenario: Reject an invalid security classification
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Create User Role With Invalid Security Classification] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-138.10
  Scenario: Reject invalid user-role live dates
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Create User Role With Invalid Live Dates] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-138.11
  Scenario: Reject a blank user role
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Create Blank User Role] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
