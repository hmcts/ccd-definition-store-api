@F-099
Feature: F-099: Search Party (Global Search)

  Background:
    Given an appropriate test context as detailed in the test data source

#  @S-099.1
#  Scenario: Must successfully import Definition file with correctly configured, single case field name in SearchPartyName column of SearchParty tab
#    Given a user with [an active profile in CCD]
#    When a request is prepared with appropriate values
#    And the request [contains correctly configured SearchParty tab with single case field name in the SearchPartyName column]
#    And the request [contains correctly configured SearchParty tab with valid field names in the SearchPartyEmailAddress, SearchPartyAddressLine1, SearchPartyPostCode and SearchPartyDob columns]
#    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
#    Then a positive response is received
#    And the response has all other details as expected
#    And a call [fetch a Case Type Schema to verify searchParties] will get the expected response as in [F-099_Get_CaseType].


  @S-099.1
  Scenario: Must successfully import Definition file with correctly configured, single case field name in SearchPartyName column of SearchParty tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected
    And a call [fetch Master Case Type Schema to verify single case field values in columns SearchPartyName(AC 1), SearchPartyEmailAddress(AC6), SearchPartyAddressLine1(AC8), SearchPartyPostCode(AC10) and SearchPartyDoB(AC12)] will get the expected response as in [S-099.1_Get_MasterCaseType].
    And a call [fetch Multiple Pages Case Type Schema to verify comma separated case field names in SearchPartyName column(AC5)] will get the expected response as in [S-099.1_Get_MultiplePagesCaseType].
    And a call [fetch Conditionals Case Type Schema to verify complex field name in columns SearchPartyName(AC3), SearchPartyEmailAddress(AC7), SearchPartyAddressLine1(AC9), SearchPartyPostCode(AC11), SearchPartyDob(AC13)] will get the expected response as in [S-099.1_Get_ConditionalsCaseType]
    And a call [fetch Retain Hidden Value Case Type Schema to verify comma separated complex field names in SearchPartyName column(AC4) and comma separated combination of top level field names and complex field names in SearchPartyName column(AC5)] will get the expected response as in [S-099.1_Get_RetainHiddenValueCaseType]
    And a call [fetch Tabs Case Type Schema to verify  blank value in the columns SearchPartyName(AC22), SearchPartyEmailAddress(AC23), SearchPartyAddressLine1(AC24), SearchPartyDoB(AC25) and SearchPartyPostCode(AC26)] will get the expected response as in [S-099.1_Get_TabsCaseType]