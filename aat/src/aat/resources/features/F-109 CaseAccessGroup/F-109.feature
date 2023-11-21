@F-109
Feature: F-109: 'CaseAccessGroup' Base Complex Type

  Background:
    Given an appropriate test context as detailed in the test data source

 @Ignore @S-109.1 #AC01
  Scenario: Must successfully import a definition file that contains CaseAccessGroup fields
    Given a user with [an active profile in CCD],
    When a request is prepared with appropriate values,
    And the request [contains correctly configured CaseAccessGroup Fields],
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store],
    Then a positive response is received,
    And the response has all other details as expected

  @Ignore @S-109.2 #AC02
  Scenario: A new case is created with CaseAccessGroup base type
  Given a user with [an active profile in CCD],
    When a request is prepared with appropriate values
   And the request [contains id of a case type with CaseAccessGroup fields with caseAccessGroupType and caseAccessGroupId],
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
  Then a positive response is received,
    And the response [contains all details of the case type requested]
    #And a call [fetch a Case Type Schema] will get the expected response as in [S-109.2_Get_CaseType].


  @S-109.3 #AC03
  Scenario: must return case type details for the request
  Given a user with [an active profile in CCD]
  When a request is prepared with appropriate values
    And the request [contains a valid jurisdiction id]
    And the request [contains a valid caseType id]
    And it is submitted to call the [Get Case Type Details] operation of [CCD Definition Store]
#  And the request [contains correctly configured event details]
  And the request [contains CaseAccessGroup base type with caseAccessGroupType and caseAccessGroupId fields]
    Then the response [has the 200 OK code]
  Then a positive response is received
    And the response has all other details as expected

  @Ignore @S-109.4 #AC04
  Scenario: Retrieve a case with CaseAccessGroup base type
  Given a user with [an active profile in CCD]
  When a request to get a case is prepared with appropriate values
#  And the request [contains correctly configured event details]
  And it is submitted to [get a case] operation of [CCD Data Store API]
  And the response [has the 200 OK code]

