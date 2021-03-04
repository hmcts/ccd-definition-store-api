#=========================================================================
@F-096
Feature: F-096: rename of UserRole column to AccessProfile 
#=========================================================================

Background:
    Given an appropriate test context as detailed in the test data source

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
@S-096.1
Scenario: must successfully import a definition file using AccessProfile columns

    Given a user with [an active profile in CCD]

     When a request is prepared with appropriate values
      And the request [contains definition file using AccessProfile columns]
      And it is submitted to call the [POST Case Definition File] operation of [CCD Definition Store]

     Then a positive response is received
      And the response has all other details as expected
      And a call [fetch a Case Type Schema to verify ACLs generated for AccessProfile values] will get the expected response as in [S-096.1_Get_CaseType].

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
@S-096.2
Scenario: must successfully import a definition file using legacy UserRole columns

    Given a user with [an active profile in CCD]

     When a request is prepared with appropriate values
      And the request [contains definition file using legacy UserRole columns]
      And it is submitted to call the [POST Case Definition File] operation of [CCD Definition Store]

     Then a positive response is received
      And the response has all other details as expected
      And a call [fetch a Case Type Schema to verify ACLs generated for UserRole values] will get the expected response as in [S-096.2_Get_CaseType].

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
@S-096.3
Scenario: must return a negative response in an attempt to import a definition file with an invalid AccessProfile column

    Given a user with [an active profile in CCD]

     When a request is prepared with appropriate values
      And the request [contains definition file using invalid AccessProfile column]
      And it is submitted to call the [POST Case Definition File] operation of [CCD Definition Store]

     Then a negative response is received
      And the response has all other details as expected

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
