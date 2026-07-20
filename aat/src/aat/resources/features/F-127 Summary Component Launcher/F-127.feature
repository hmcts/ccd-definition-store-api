@F-127
Feature: F-127: New DefaultFocus Column

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-127.1
  Scenario: Import Definition file with true value in DefaultFocus column of the CaseTypeTab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains id of a case type with Summary base type fields]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-127.2
  Scenario: Return the new Summary base type in the Get Case Type Definition Store operation
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains id of a case type with Summary base type fields]
    And it is submitted to call the [Get Case Type Details] operation of [CCD Definition Store]
    Then a positive response is received
    And the response [contains all details of the case type requested]
    And the response has all other details as expected








#  @S-127.3
#  Scenario: Successful response for case type id with defaultFocus set on tab
#    Given a user with [an active profile in CCD]
#    When a request is prepared with appropriate values
#    And the request [contains a definition file with new column DefaultFocus]
#    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
#    Then a positive response is received
#    And the response has all other details as expected
#    And a call [get tabs for case type id which has defaultFocus value set] will get the expected response as in [S-127.3_Get_Tab_Structure]
#
#  @S-127.4
#  Scenario: Successful response for case type id with no defaultFocus column
#    Given a user with [an active profile in CCD]
#    When a request is prepared with appropriate values
#    And the request [contains a definition file with no new column DefaultFocus]
#    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
#    Then a positive response is received
#    And the response has all other details as expected
#    And a call [get tabs for case type id without newly added default_focus value] will get the expected response as in [S-127.4_Get_Tab_Structure]
#
