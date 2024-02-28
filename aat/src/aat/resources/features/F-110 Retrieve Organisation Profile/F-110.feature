@F-110
Feature: F-110: Retrieve Access Types

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-110.1 
  Scenario: Successfully retrieve access types for provided organisationProfileIds
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured values]
    And the request [contains an organisationProfileId that exists in CCD database]
    And it is submitted to call the [Retrieve Access Types] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected
    And the response [contains all accessTypes for organisationProfileId in the response]

  @S-110.2 
  Scenario: Successfully return 200 success without content for non-existent organisationProfileId
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured values]
    And the request [contains an organisationProfileId that does not exists in CCD database]
    And it is submitted to call the [Retrieve Access Types] operation of [CCD Definition Store]
    Then a positive response is received
    And the response [does not contain any accessTypes]

  @S-110.3
  Scenario: Successfully return 200 success with content for request without organisationProfileId
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured values]
    And the request [does not contain organisationProfileId]
    And it is submitted to call the [Retrieve Access Types] operation of [CCD Definition Store]
    Then a positive response is received
    And the response [contains all accessTypes for all Organisations]

  @S-110.4
  Scenario: Negative response - Return 400 Bad Request for invalid organisationProfileId
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured values]
    And the request [contains an invalid organisationProfileId]
    And it is submitted to call the [Retrieve Access Types] operation of [CCD Definition Store]
    Then a negative response is received
    And the response [has a status of 400 Bad Request]

  @S-110.5
  Scenario: Negative response - Return 401 Unauthorised for invalid S2S token
    Given a user with [a profile not active in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured values]
    And the request [contains an invalid S2S token]
    And it is submitted to call the [Retrieve Access Types] operation of [CCD Definition Store]
    Then a negative response is received
    And the response [has a status of 401 Unauthorised]

  @S-110.6 @Ignore # To be added with multiple Befta Jurisdictions
  Scenario: Successfully return 200 success with the latest version of accessTypes for organisationProfileId
    Given a user with [an active profile in CCD]
    And a pre-condition that multiple versions of AccessTypeRoles exist in the database
    When a request is prepared with appropriate values
    And the request [contains correctly configured values]
    And the request [contains an organisationProfileId that exists in CCD database]
    And it is submitted to call the [Retrieve Access Types] operation of [CCD Definition Store]
    Then a positive response is received
    And the response [has a status of 200 success]
    And the response [contains the latest version of the accessTypes for organisationProfileId in the response]

  @S-110.7 @Ignore # To be added with multiple Befta Jurisdictions
  Scenario: Successfully return 200 success with access type of all organisations across jurisdictions
    Given a user with [an active profile in CCD]
    And a pre-condition that multiple jurisdictions with their own access types exist due to imported definition files
    When a request is prepared with appropriate values
    And the request [contains correctly configured values]
    And the request [does not contain organisationProfileId]
    And it is submitted to call the [Retrieve Access Types] operation of [CCD Definition Store]
    Then a positive response is received
    And the response [has a status of 200 success]
    And the response [contains the latest version of the accessTypes for all organisations across jurisdictions]
