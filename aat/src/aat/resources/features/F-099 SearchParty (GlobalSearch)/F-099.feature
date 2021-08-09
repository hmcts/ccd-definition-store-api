@F-099
Feature: F-099: Search Party (Global Search)

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-099.1
  Scenario: Must successfully import Definition file with correctly configured, single case field name in SearchPartyName column of SearchParty tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured SearchParty tab with single case field name in the SearchPartyName column]
    And the request [contains correctly configured SearchParty tab with valid field names in the SearchPartyEmailAddress, SearchPartyAddressLine1, SearchPartyPostCode and SearchPartyDob columns]
    And it is submitted to call the [Import Definition File] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected
    And a call [to verify that the definition is created in the definition store database] will get the expected response as in [YYY].
