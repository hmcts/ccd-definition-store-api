@F-110
Feature: F-110: Retrieve Access Types

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-110.1 @Ignore # Response mismatch, has unexpected number of elements. Expected: 1, but actual: 2 due to aat db containing bad data, CCD-6078
  Scenario: Successfully retrieve access types for provided organisationProfileIds
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured values]
    And the request [contains an organisationProfileId that exists in CCD database]
    And it is submitted to call the [Retrieve Access Types] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected
    And the response [contains all accessTypes for organisationProfileId in the response]

  @S-110.1a @Ignore # Response mismatch, has unexpected number of elements. Expected: 1, but actual: 2 due to aat db containing bad data, CCD-6078, #AC-1a of CCD-5322)
  Scenario: Successfully return 200 success with content for request access type of the organisation and only latest version of the AccessTypes
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured values]
    And the request [contains an organisationProfileId that exists in CCD database]
    And the request [contains an organisationProfileId and accessTypes exists in CCD database]
    And it is submitted to call the [Retrieve Access Types] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected
    And the response [contains the latest version of the accessTypes for organisationProfileId]

  @S-110.1b #AC-1a of CCD-5322
  Scenario: Successfully return 200 success multiple jurisdiction and each case type under different jurisdiction will have their only access types
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured values]
    And the request [contains an organisationProfileId that exists in CCD database]
    And the request [contains an organisationProfileId and accessTypes exists in CCD database]
    And it is submitted to call the [Retrieve Access Types] operation of [CCD Definition Store]
    Then a positive response is received
    And the response [contains the latest version of the accessTypes for all organisations across jurisdictions]

  @S-110.2
  Scenario: Successfully return 200 success without content for non-existent organisationProfileId
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured values]
    And the request [contains an organisationProfileId that does not exists in CCD database]
    And it is submitted to call the [Retrieve Access Types] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected
    And the response [does not contain any accessTypes]

  @S-110.3 @Ignore # Response mismatch, actualResponse.body contains a bad value, due to aat db containing bad data, CCD-6078
  Scenario: Successfully return 200 success with content for request without organisationProfileId
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured values]
    And the request [does not contain organisationProfileId]
    And it is submitted to call the [Retrieve Access Types] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected
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

  @S-110.6 # more than one version of AccessTypeRoles
  Scenario: Successfully return 200 success accessTypes of organisationProfileId and only latest version of AccessTypes
    Given a user with [an active profile in CCD]
    And a call [to import definition file, multiple versions] will get the expected response as in [Import_CCD_BEFTA_RM_CT_JURISDICTION1]
    When a request is prepared with appropriate values
    And the request [contains correctly configured values]
    And the request [contains an organisationProfileId that exists in CCD database]
    And the request [contains an organisationProfileId and accessTypes exists in CCD database]
    And it is submitted to call the [Retrieve Access Types] operation of [CCD Definition Store]
    Then a positive response is received
    And the response [contains the latest version of the accessTypes for organisationProfileId]

