#=============================
@F-128
Feature: F-128: Fetch Import Audits
#=============================

Background:
    Given an appropriate test context as detailed in the test data source

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
@S-128.1
Scenario: must return a successful response containing the import audits

    Given a user with [an active profile in CCD]

     When a request is prepared with appropriate values
      And it is submitted to call the [Fetch Import Audits] operation of [CCD Definition Store]

     Then the response [has the 200 OK code]
      And the response [contains the list of import audits]
      And the response has all other details as expected

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
@S-128.2
Scenario: must return 401 when request does not provide valid authentication credentials

    Given a user with [an active profile in CCD]

     When a request is prepared with appropriate values
      And the request [contains an invalid authentication credentials]
      And it is submitted to call the [Fetch Import Audits] operation of [CCD Definition Store]

     Then a negative response is received
      And the response [contains 401 unauthorised code]
      And the response has all other details as expected

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
