@F-132
Feature: F-132: Additional API endpoint coverage

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-132.1
  Scenario: Return banners for a jurisdiction
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get Banners] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-132.2
  Scenario: Return empty banners when jurisdictions are omitted
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get Banners Without Jurisdictions] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-132.3
  Scenario: Return jurisdiction UI configs
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get Jurisdiction UI Configs] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-132.4
  Scenario: Return empty jurisdiction UI configs when jurisdictions are omitted
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get Jurisdiction UI Configs Without Jurisdictions] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-132.5
  Scenario: Return search cases result fields
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get Search Cases Result Fields] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-132.6
  Scenario: Return the IDAM profile
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get IDAM Profile] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-132.7
  Scenario: Return the IDAM profile roles
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get IDAM Profile Roles] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-132.8
  Scenario: Return admin web authorisation
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get Admin Web Authorisation] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-132.9
  Scenario: Return import audits
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get Import Audits] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-132.10
  Scenario: Retrieve an existing user role
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get User Role] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-132.11
  Scenario: Return not found for an unknown user role
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get Unknown User Role] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-132.12
  Scenario: Reject an invalid Base64 user role query parameter
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get User Role With Invalid Base64] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-132.13
  Scenario: Reject a missing user role query parameter
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get User Role Without Role] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-132.14
  Scenario: Reject a draft request without a jurisdiction
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Create Draft Without Jurisdiction] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-132.15
  Scenario: Reject saving a draft without a description
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Save Draft Without Description] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-132.16
  Scenario: Reject saving a draft without an author
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Save Draft Without Author] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-132.17
  Scenario: Reject unauthenticated display requests
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get Banners Without Valid Authentication] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-132.18
  Scenario: Reject unauthenticated IDAM requests
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get IDAM Profile Without Valid Authentication] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-132.19
  Scenario: Reject unauthenticated import-audit requests
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get Import Audits Without Valid Authentication] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-132.20
  Scenario: Reject unauthenticated user-role requests
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get User Role Without Valid Authentication] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-132.21
  Scenario: Reject unauthenticated draft requests
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Create Draft Without Valid Authentication] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-132.22
  Scenario: Reject an unknown case type for search-case result fields
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get Search Cases Result Fields For Unknown Case Type] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-132.23
  Scenario: Reject deletion of an unknown draft version
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Delete Unknown Draft Version] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-132.24
  Scenario: Reject malformed draft JSON
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Create Draft With Malformed JSON] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-132.25
  Scenario: Reject creation of a duplicate user role
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Create Duplicate User Role] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-132.26
  Scenario: Reject a user role without a role value
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Create User Role Without Role] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-132.27
  Scenario: Reject a user role without a security classification
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Create User Role Without Security Classification] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-132.28
  Scenario: Reject an invalid security classification
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Create User Role With Invalid Security Classification] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-132.29
  Scenario: Reject invalid user-role live dates
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Create User Role With Invalid Live Dates] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-132.30
  Scenario: Reject malformed user-role JSON
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Create User Role With Malformed JSON] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-132.31
  Scenario: Reject an unsupported user-role content type
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Create User Role With Unsupported Content Type] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-132.32
  Scenario: Reject a blank user role
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Create Blank User Role] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
