#=========================================
@F-127
Feature: F-127: Get Case Roles for a Case Type
#=========================================

Background:
    Given an appropriate test context as detailed in the test data source

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
@S-127.1
Scenario: must return the list of case roles for a given case type

    Given a user with [an active profile in CCD]

     When a request is prepared with appropriate values
      And the request [contains a valid case type id]
      And it is submitted to call the [Get Case Roles for a Case Type] operation of [CCD Definition Store]

     Then the response [has the 200 OK code]
      And the response [contains the case roles defined for the case type]
      And the response has all other details as expected

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
@S-127.2
Scenario: must return 401 when request does not provide valid authentication credentials

    Given a user with [an active profile in CCD]

     When a request is prepared with appropriate values
      And the request [contains an invalid authentication credentials]
      And it is submitted to call the [Get Case Roles for a Case Type] operation of [CCD Definition Store]

     Then a negative response is received
      And the response [contains 401 unauthorised code]
      And the response has all other details as expected

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
@S-127.3
Scenario: must return 404 when request provides a non-existing case type id

    Given a user with [an active profile in CCD]

     When a request is prepared with appropriate values
      And the request [contains a non-existing case type id]
      And it is submitted to call the [Get Case Roles for a Case Type] operation of [CCD Definition Store]

     Then a negative response is received
      And the response [contains 404 not found code]
      And the response has all other details as expected

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
