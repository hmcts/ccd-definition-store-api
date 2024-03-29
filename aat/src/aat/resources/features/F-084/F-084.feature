#==================================================================
@F-084
Feature: F-084: Fetch UI Definition for Search Results by Case Type
#==================================================================

Background:
    Given an appropriate test context as detailed in the test data source

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
@S-364
Scenario: must return the UI definition of search results for a given case type

    Given a user with [an active profile in CCD]

     When a request is prepared with appropriate values
      And the request [contains a valid case type id]
      And it is submitted to call the [Fetch UI definition for Search Results By CaseType] operation of [CCD Definition Store]

     Then the response [has the 200 OK code]
      And the response [has Search Results definition details]
      And the response has all other details as expected

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
@S-361
Scenario: must return 401 when request does not provide valid authentication credentials

    Given a user with [an active profile in CCD]

     When a request is prepared with appropriate values
      And the request [contains an invalid authentication credentials]
      And it is submitted to call the [Fetch UI definition for Search Results By CaseType] operation of [CCD Definition Store]

     Then a negative response is received
      And the response [contains 401 unauthorised code]
      And the response has all other details as expected

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
@S-362 @Ignore # Response code mismatch, expected: 403, actual: 200 CCD-4455
Scenario: must return 403 when request provides authentic credentials without authorised access to the operation 

    Given a user with [an active profile in CCD, and insufficient privilege to the case type]

     When a request is prepared with appropriate values
      And it is submitted to call the [Fetch UI definition for Search Results By CaseType] operation of [CCD Definition Store]

     Then a negative response is received
      And the response [contains 403 forbidden code]
      And the response has all other details as expected

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
@S-363 @Ignore # Response code mismatch, expected: 404, actual: 200 CCD-4461
Scenario: must return 404 when request provides a non-existing case type id

    Given a user with [an active profile in CCD]

     When a request is prepared with appropriate values
      And the request [contains a non-existing case type id]
      And it is submitted to call the [Fetch UI definition for Search Results By CaseType] operation of [CCD Definition Store]

     Then a negative response is received
      And the response [contains 404 not found code]
      And the response has all other details as expected

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
