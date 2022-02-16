@F-099
Feature: F-099: Search Party (Global Search)

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-099.1 #AC-1, AC-2, AC-3, AC-4, AC-5, AC-6, AC-7, AC-8, AC-9, AC-10, AC-11, AC-12, AC-13, AC-25, AC-26, AC-27, AC-28, AC-29
  Scenario: Import Definition file with correctly configured, single case field name in SearchPartyName column of SearchParty tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected
    And a call [fetch Complex Fields Ordering Schema to verify single case field values in columns SearchPartyName(AC1), SearchPartyEmailAddress(AC6), SearchPartyAddressLine1(AC8), SearchPartyPostCode(AC10) and SearchPartyDoB(AC12)] will get the expected response as in [S-099.1_Get_ComplexFieldsOrdering].
    And a call [fetch Multiple Pages Case Type Schema to verify comma separated case field names in SearchPartyName column(AC5)] will get the expected response as in [S-099.1_Get_MultiplePagesCaseType].
    And a call [fetch Conditionals Case Type Schema to verify complex field name in columns SearchPartyName(AC3), SearchPartyEmailAddress(AC7), SearchPartyAddressLine1(AC9), SearchPartyPostCode(AC11), SearchPartyDob(AC13)] will get the expected response as in [S-099.1_Get_ConditionalsCaseType]
    And a call [fetch Retain Hidden Value Case Type Schema to verify comma separated complex field names in SearchPartyName column(AC4) and comma separated combination of top level field names and complex field names in SearchPartyName column(AC5)] will get the expected response as in [S-099.1_Get_RetainHiddenValueCaseType]
    And a call [fetch Tabs Case Type Schema to verify blank value in the columns SearchPartyName(AC25), SearchPartyEmailAddress(AC26), SearchPartyAddressLine1(AC27), SearchPartyDoB(AC28), SearchPartyPostCode(AC29) and SearchPartyCollectionFieldName] will get the expected response as in [S-099.1_Get_TabsCaseType]
    And a call [fetch Global Search Case Type Schema to verify blank value in the columns SearchPartyCollectionFieldName] will get the expected response as in [S-099.1_Get_GlobalSearchCaseType]

  @S-099.2 #AC-14
  Scenario: Import Definition file with in-correct value in "CaseTypeId" column of the SearchParty tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains an invalid value (not matching the CaseType tab) in the configured 'CaseTypeId' column of the SearchParty tab]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
    And a call [to fetch incorrect caseTypeId schema] will get the expected response as in [S-099.2_Get_InvalidCaseType].

  @S-099.3 #AC-15
  Scenario: Import Definition file with invalid complex case field name in the "SearchPartyDoB" column of the SearchParty tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured SearchParty tab with valid case field names in the SearchPartyName column]
    And the request [contains correctly configured SearchParty tab with valid case field names in SearchPartyEmailAddress, SearchPartyAddressLine1 and SearchPartyPostCode columns]
    And the request [contains invalid complex field name in the SearchPartyDoB column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
    And a call [verify search party data has not been updated] will get the expected response as in [F-099_VerifyComplexFieldsOrderingSnapshotUnchanged].

  @S-099.4 #AC-16
  Scenario: Import Definition file with invalid top level case field name in the "SearchPartyDoB" column of the SearchParty tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured SearchParty tab with valid case field names in the SearchPartyName column]
    And the request [contains correctly configured SearchParty tab with valid case field names in SearchPartyEmailAddress, SearchPartyAddressLine1 and SearchPartyPostCode columns]
    And the request [contains invalid top level case field name in the SearchPartyDoB column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
    And a call [verify search party data has not been updated] will get the expected response as in [F-099_VerifyComplexFieldsOrderingSnapshotUnchanged].

  @S-099.5 #AC-17
  Scenario: Import Definition file with invalid complex case field name in the "SearchPartyPostCode" column of the SearchParty tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured SearchParty tab with valid case field names in the SearchPartyName column]
    And the request [contains correctly configured SearchParty tab with valid case field names in SearchPartyEmailAddress, SearchPartyAddressLine1 and SearchPartyDob columns]
    And the request [contains invalid complex case field name in the SearchPartyPostCode column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
    And a call [verify search party data has not been updated] will get the expected response as in [F-099_VerifyComplexFieldsOrderingSnapshotUnchanged].

  @S-099.6 #AC-18
  Scenario:  Import Definition file with invalid top level case field name in the "SearchPartyPostCode" column of the SearchParty tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured SearchParty tab with valid case field names in the SearchPartyName column]
    And the request [contains correctly configured SearchParty tab with valid case field names in SearchPartyEmailAddress, SearchPartyAddressLine1 and SearchPartyDob columns]
    And the request [contains invalid top level case field name in the SearchPartyPostCode column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
    And a call [verify search party data has not been updated] will get the expected response as in [F-099_VerifyComplexFieldsOrderingSnapshotUnchanged].

  @S-099.7 #AC-19
  Scenario: Import Definition file with invalid complex case field name in the "SearchPartyAddressLine1" column of the SearchParty tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured SearchParty tab with valid case field names in the SearchPartyName column]
    And the request [contains correctly configured SearchParty tab with valid case field names in SearchPartyEmailAddress, SearchPartyPostCode and SearchPartyDob columns]
    And the request [contains invalid complex case field name in the SearchPartyAddressLine1 column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
    And a call [verify search party data has not been updated] will get the expected response as in [F-099_VerifyComplexFieldsOrderingSnapshotUnchanged].

  @S-099.8 #AC-20
  Scenario: Import Definition file with invalid top level case field name in the "SearchPartyAddressLine1" column of the SearchParty tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured SearchParty tab with valid case field names in the SearchPartyName column]
    And the request [contains correctly configured SearchParty tab with valid case field names in SearchPartyEmailAddress, SearchPartyPostCode and SearchPartyDob columns]
    And the request [contains invalid top level case field name in the SearchPartyAddressLine1 column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
    And a call [verify search party data has not been updated] will get the expected response as in [F-099_VerifyComplexFieldsOrderingSnapshotUnchanged].

  @S-099.9 #AC-21
  Scenario: Import Definition file with invalid complex case field name in the "SearchPartyEmailAddress" column of the SearchParty tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured SearchParty tab with valid case field names in the SearchPartyName column]
    And the request [contains correctly configured SearchParty tab with valid case field names in SearchPartyAddressLine1, SearchPartyPostCode and SearchPartyDob columns]
    And the request [contains invalid complex field name in the SearchPartyEmailAddress column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
    And a call [verify search party data has not been updated] will get the expected response as in [F-099_VerifyComplexFieldsOrderingSnapshotUnchanged].

  @S-099.10 #AC-22
  Scenario: Import Definition file with invalid top level case field name in the "SearchPartyEmailAddress" column of the SearchParty tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured SearchParty tab with valid case field names in the SearchPartyName column]
    And the request [contains correctly configured SearchParty tab with valid case field names in SearchPartyAddressLine1, SearchPartyPostCode and SearchPartyDob columns]
    And the request [contains invalid top level case field name in the SearchPartyEmailAddress column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
    And a call [verify search party data has not been updated] will get the expected response as in [F-099_VerifyComplexFieldsOrderingSnapshotUnchanged].

  @S-099.11 #AC-23
  Scenario: Import Definition file with invalid complex case field name in the "SearchPartyName" column of the SearchParty tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains SearchParty tab with a invalid case field name in the SearchPartyName column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
    And a call [verify search party data has not been updated] will get the expected response as in [F-099_VerifyComplexFieldsOrderingSnapshotUnchanged].

  @S-099.12 #AC-24
  Scenario: Import Definition file with invalid top level case field name in the "SearchPartyName" column of the SearchParty tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains SearchParty tab with a invalid complex element name in the SearchPartyName column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
    And a call [verify search party data has not been updated] will get the expected response as in [F-099_VerifyComplexFieldsOrderingSnapshotUnchanged].

  @S-099.13
  Scenario: Import Definition file with invalid complex case field name in the "SearchPartyCollectionFieldName" column of the SearchParty tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured SearchParty tab with valid case field names in the SearchPartyName column]
    And the request [contains correctly configured SearchParty tab with valid case field names in SearchPartyEmailAddress, SearchPartyDoB, SearchPartyAddressLine1 and SearchPartyPostCode columns]
    And the request [contains invalid complex field name in the SearchPartyCollectionFieldName column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
    And a call [verify search party data has not been updated] will get the expected response as in [F-099_VerifyComplexFieldsOrderingSnapshotUnchanged].

  @S-099.14
  Scenario: Import Definition file with invalid top level case field name in the "SearchPartyCollectionFieldName" column of the SearchParty tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured SearchParty tab with valid case field names in the SearchPartyName column]
    And the request [contains correctly configured SearchParty tab with valid case field names in SearchPartyEmailAddress, SearchPartyDoB, SearchPartyAddressLine1 and SearchPartyPostCode columns]
    And the request [contains invalid top level case field name in the SearchPartyCollectionFieldName column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
    And a call [verify search party data has not been updated] will get the expected response as in [F-099_VerifyComplexFieldsOrderingSnapshotUnchanged].

  @S-099.15
  Scenario: Import Definition file with a non-collection type in the "SearchPartyCollectionFieldName" column of the SearchParty tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured SearchParty tab with valid case field names in the SearchPartyName column]
    And the request [contains correctly configured SearchParty tab with valid case field names in SearchPartyEmailAddress, SearchPartyDoB, SearchPartyAddressLine1 and SearchPartyPostCode columns]
    And the request [contains non-collection type in the SearchPartyCollectionFieldName column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
    And a call [verify search party data has not been updated] will get the expected response as in [F-099_VerifyGlobalSearchSchemaSnapshotUnchanged].

  @S-099.16
  Scenario: Import Definition file with a non-complex type referenced in the "SearchPartyCollectionFieldName" column of the SearchParty tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured SearchParty tab with valid case field names in the SearchPartyName column]
    And the request [contains correctly configured SearchParty tab with valid case field names in SearchPartyEmailAddress, SearchPartyDoB, SearchPartyAddressLine1 and SearchPartyPostCode columns]
    And the request [contains non-complex type referenced in the SearchPartyCollectionFieldName column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
    And a call [verify search party data has not been updated] will get the expected response as in [F-099_VerifyGlobalSearchSchemaSnapshotUnchanged].

  @S-099.17
  Scenario: Import Definition file with a valid collection field in the "SearchPartyCollectionFieldName" and invalid complex case field name in the "SearchPartyDoB" column of the SearchParty tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured SearchParty tab with valid case field name in the SearchPartyCollectionFieldName column]
    And the request [contains correctly configured SearchParty tab with valid case field names in SearchPartyName, SearchPartyEmailAddress, SearchPartyDoD, SearchPartyAddressLine1 and SearchPartyPostCode columns]
    And the request [contains invalid complex field name in the SearchPartyDoB column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
    And a call [verify search party data has not been updated] will get the expected response as in [F-099_VerifyGlobalSearchSchemaSnapshotUnchanged].

  @S-099.18
  Scenario: Import Definition file with a valid collection field in the "SearchPartyCollectionFieldName" and invalid top level case field name in the "SearchPartyDoB" column of the SearchParty tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured SearchParty tab with valid case field name in the SearchPartyCollectionFieldName column]
    And the request [contains correctly configured SearchParty tab with valid case field names in SearchPartyName, SearchPartyEmailAddress, SearchPartyDoD, SearchPartyAddressLine1 and SearchPartyPostCode columns]
    And the request [contains invalid top level case field name in the SearchPartyDoB column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
    And a call [verify search party data has not been updated] will get the expected response as in [F-099_VerifyGlobalSearchSchemaSnapshotUnchanged].

  @S-099.19
  Scenario: Import Definition file with a valid collection field in the "SearchPartyCollectionFieldName" and invalid complex case field name in the "SearchPartyPostCode" column of the SearchParty tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured SearchParty tab with valid case field name in the SearchPartyCollectionFieldName column]
    And the request [contains correctly configured SearchParty tab with valid case field names in SearchPartyName, SearchPartyEmailAddress, SearchPartyDoD, SearchPartyAddressLine1 and SearchPartyDob columns]
    And the request [contains invalid complex case field name in the SearchPartyPostCode column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
    And a call [verify search party data has not been updated] will get the expected response as in [F-099_VerifyGlobalSearchSchemaSnapshotUnchanged].

  @S-099.20
  Scenario:  Import Definition file with a valid collection field in the "SearchPartyCollectionFieldName" and invalid top level case field name in the "SearchPartyPostCode" column of the SearchParty tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured SearchParty tab with valid case field name in the SearchPartyCollectionFieldName column]
    And the request [contains correctly configured SearchParty tab with valid case field names in SearchPartyName, SearchPartyEmailAddress, SearchPartyDoD, SearchPartyAddressLine1 and SearchPartyDob columns]
    And the request [contains invalid top level case field name in the SearchPartyPostCode column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
    And a call [verify search party data has not been updated] will get the expected response as in [F-099_VerifyGlobalSearchSchemaSnapshotUnchanged].

  @S-099.21
  Scenario: Import Definition file with a valid collection field in the "SearchPartyCollectionFieldName" and invalid complex case field name in the "SearchPartyAddressLine1" column of the SearchParty tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured SearchParty tab with valid case field name in the SearchPartyCollectionFieldName column]
    And the request [contains correctly configured SearchParty tab with valid case field names in SearchPartyName, SearchPartyEmailAddress, SearchPartyDoD, SearchPartyPostCode and SearchPartyDob columns]
    And the request [contains invalid complex case field name in the SearchPartyAddressLine1 column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
    And a call [verify search party data has not been updated] will get the expected response as in [F-099_VerifyGlobalSearchSchemaSnapshotUnchanged].

  @S-099.22
  Scenario: Import Definition file with a valid collection field in the "SearchPartyCollectionFieldName" and invalid top level case field name in the "SearchPartyAddressLine1" column of the SearchParty tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured SearchParty tab with valid case field name in the SearchPartyCollectionFieldName column]
    And the request [contains correctly configured SearchParty tab with valid case field names in SearchPartyName, SearchPartyEmailAddress, SearchPartyDoD, SearchPartyPostCode and SearchPartyDob columns]
    And the request [contains invalid top level case field name in the SearchPartyAddressLine1 column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
    And a call [verify search party data has not been updated] will get the expected response as in [F-099_VerifyGlobalSearchSchemaSnapshotUnchanged].

  @S-099.23
  Scenario: Import Definition file with a valid collection field in the "SearchPartyCollectionFieldName" and invalid complex case field name in the "SearchPartyEmailAddress" column of the SearchParty tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured SearchParty tab with valid case field name in the SearchPartyCollectionFieldName column]
    And the request [contains correctly configured SearchParty tab with valid case field names in SearchPartyName, SearchPartyAddressLine1, SearchPartyDoD, SearchPartyPostCode and SearchPartyDob columns]
    And the request [contains invalid complex field name in the SearchPartyEmailAddress column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
    And a call [verify search party data has not been updated] will get the expected response as in [F-099_VerifyGlobalSearchSchemaSnapshotUnchanged].

  @S-099.24
  Scenario: Import Definition file with a valid collection field in the "SearchPartyCollectionFieldName" and invalid top level case field name in the "SearchPartyEmailAddress" column of the SearchParty tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured SearchParty tab with valid case field name in the SearchPartyCollectionFieldName column]
    And the request [contains correctly configured SearchParty tab with valid case field names in SearchPartyName, SearchPartyAddressLine1, SearchPartyDoD, SearchPartyPostCode and SearchPartyDob columns]
    And the request [contains invalid top level case field name in the SearchPartyEmailAddress column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
    And a call [verify search party data has not been updated] will get the expected response as in [F-099_VerifyGlobalSearchSchemaSnapshotUnchanged].

  @S-099.25
  Scenario: Import Definition file with a valid collection field in the "SearchPartyCollectionFieldName" and invalid complex case field name in the "SearchPartyName" column of the SearchParty tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured SearchParty tab with valid case field name in the SearchPartyCollectionFieldName column]
    And the request [contains correctly configured SearchParty tab with valid case field names in SearchPartyEmailAddress, SearchPartyAddressLine1, SearchPartyDoD, SearchPartyPostCode and SearchPartyDob columns]
    And the request [contains invalid complex field name in the SearchPartyName column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
    And a call [verify search party data has not been updated] will get the expected response as in [F-099_VerifyGlobalSearchSchemaSnapshotUnchanged].

  @S-099.26
  Scenario: Import Definition file with a valid collection field in the "SearchPartyCollectionFieldName" and invalid top level case field name in the "SearchPartyName" column of the SearchParty tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured SearchParty tab with valid case field name in the SearchPartyCollectionFieldName column]
    And the request [contains correctly configured SearchParty tab with valid case field names in SearchPartyEmailAddress, SearchPartyAddressLine1, SearchPartyDoD, SearchPartyPostCode and SearchPartyDob columns]
    And the request [contains invalid top level case field name in the SearchPartyName column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
    And a call [verify search party data has not been updated] will get the expected response as in [F-099_VerifyGlobalSearchSchemaSnapshotUnchanged].

  @S-099.27
  Scenario: Import Definition file with a valid collection field in the "SearchPartyCollectionFieldName" and invalid complex case field name in the "SearchPartyDoD" column of the SearchParty tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured SearchParty tab with valid case field name in the SearchPartyCollectionFieldName column]
    And the request [contains correctly configured SearchParty tab with valid case field names in SearchPartyName, SearchPartyEmailAddress, SearchPartyDoB, SearchPartyAddressLine1 and SearchPartyPostCode columns]
    And the request [contains invalid complex field name in the SearchPartyDoD column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
    And a call [verify search party data has not been updated] will get the expected response as in [F-099_VerifyGlobalSearchSchemaSnapshotUnchanged].

  @S-099.28
  Scenario: Import Definition file with a valid collection field in the "SearchPartyCollectionFieldName" and invalid top level case field name in the "SearchPartyDoD" column of the SearchParty tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains correctly configured SearchParty tab with valid case field name in the SearchPartyCollectionFieldName column]
    And the request [contains correctly configured SearchParty tab with valid case field names in SearchPartyName, SearchPartyEmailAddress, SearchPartyDoB, SearchPartyAddressLine1 and SearchPartyPostCode columns]
    And the request [contains invalid top level case field name in the SearchPartyDoD column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
    And a call [verify search party data has not been updated] will get the expected response as in [F-099_VerifyGlobalSearchSchemaSnapshotUnchanged].

