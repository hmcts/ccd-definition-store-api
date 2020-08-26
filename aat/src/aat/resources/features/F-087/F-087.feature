#======================================================================
@F-087
Feature: F-087: Fetch UI Definition for Work Basket Inputs by Case Type
#======================================================================

Background:
    Given an appropriate test context as detailed in the test data source

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
@S-380
Scenario: must return the UI definition of work basket inputs for a given case type

    Given a user with [an active profile in CCD]

     When a request is prepared with appropriate values
      And the request [contains a valid case type id]
      And it is submitted to call the [Fetch UI definition for Work Basket Inputs By CaseType] operation of [CCD Definition Store]

     Then the response [has the 200 OK code]
      And the response [has Work Basket input definition details]
      And the response has all other details as expected

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
@S-377 @Ignore @RDM-7618
Scenario: must return 401 when request does not provide valid authentication credentials

    Given a user with [an active profile in CCD]

     When a request is prepared with appropriate values
      And the request [contains an invalid authentication credentials]
      And it is submitted to call the [Fetch UI definition for Work Basket Inputs By CaseType] operation of [CCD Definition Store]

     Then a negative response is received
      And the response [contains 401 unauthorised code]
      And the response has all other details as expected

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
@S-378 @Ignore  @RDM-7618
Scenario: must return 403 when request provides authentic credentials without authorised access to the operation 

    Given a user with [an active profile in CCD, and insufficient privilege to the case type]

     When a request is prepared with appropriate values
      And it is submitted to call the [Fetch UI definition for Work Basket Inputs By CaseType] operation of [CCD Definition Store]

     Then a negative response is received
      And the response [contains 403 forbidden code]
      And the response has all other details as expected

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
@S-379 @Ignore  @RDM-7618
Scenario: must return 404 when request provides a non-existing case type id

    Given a user with [an active profile in CCD]

     When a request is prepared with appropriate values
      And the request [contains a non-existing case type id]
      And it is submitted to call the [Fetch UI definition for Work Basket Inputs By CaseType] operation of [CCD Definition Store]

     Then a negative response is received
      And the response [contains 404 not found code]
      And the response has all other details as expected

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
