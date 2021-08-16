@F-100
Feature: F-100: Search Criteria (Global Search)

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-100.1 @AC-1 @AC-2 @AC-3 @AC-7
  Scenario: Must successfully import Definition file with correctly configured, single case field name in OtherCaseReference column of SearchCriteria tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected
    And a call [fetch Master Case Type Schema to verify single case field values in columns OtherCaseReference (AC1)] will get the expected response as in [S-100.1_Get_MasterCaseType].
    And a call [fetch Multiple Pages Case Type Schema to verify comma separated case field names in OtherCaseReference (AC2) column] will get the expected response as in [S-100.1_Get_MultiplePagesCaseType].
    And a call [fetch Conditionals Case Type Schema to verify complex field name in columns OtherCaseReference (AC3)] will get the expected response as in [S-100.1_Get_ConditionalsCaseType]
    And a call [fetch Tabs Case Type Schema to verify blank value in the columns OtherCaseReference (AC7)] will get the expected response as in [S-100.1_Get_TabsCaseType]

  @S-100.2 @AC-4
  Scenario: Import Definition file with in-correct value in "CaseTypeId" column of the SearchCriteria tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains an invalid value (not matching the CaseType tab) in the configured 'CaseTypeId' column of the SearchCriteria tab]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
    And a call [to fetch incorrect caseTypeId schema] will get the expected response as in [S-100.2_Get_InvalidCaseType].

  @S-100.3 @AC-5
  Scenario: Import Definition file with invalid case field name in the "OtherCaseReference" column of the SearchCriteria tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains SearchCriteria tab with a invalid case field name in the OtherCaseReference column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
    And a call [verify search criteria data has not been updated] will get the expected response as in [F-100_VerifyComplexFieldsOrderingSnapshotUnchanged].

  @S-100.4 @AC-6
  Scenario: Import Definition file with invalid complex case field name in the "OtherCaseReference" column of the SearchCriteria tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains invalid complex case field name in the OtherCaseReference column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
    And a call [verify search criteria data has not been updated] will get the expected response as in [F-100_VerifyComplexFieldsOrderingSnapshotUnchanged].

