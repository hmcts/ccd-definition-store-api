#====================================
@F-113
Feature: F-113: Import Job Tracking
#====================================

Background:
    Given an appropriate test context as detailed in the test data source

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
@S-113.1
Scenario: must successfully import a definition file with a supplied job id and retrieve its status

    Given a user with [an active profile in CCD]

     When a request is prepared with appropriate values
      And the request [contains a supplied X-Import-Job-Id request header]
      And it is submitted to call the [Import definition file] operation of [CCD Definition Store]

     Then a positive response is received
      And the response has all other details as expected
      And a call [to retrieve the import job status] will get the expected response as in [S-113.1_GetImportJob].

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
@S-113.2
Scenario: must return 404 when retrieving an import job with a non-existent id

    Given a user with [an active profile in CCD]

     When a request is prepared with appropriate values
      And the request [contains a syntactically valid but non-existent UUID as the job id]
      And it is submitted to call the [Get Import Job] operation of [CCD Definition Store]

     Then a negative response is received
      And the response has all other details as expected

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
@S-113.3
Scenario: must return 400 when retrieving an import job with a malformed id

    Given a user with [an active profile in CCD]

     When a request is prepared with appropriate values
      And the request [contains a non-UUID value as the job id]
      And it is submitted to call the [Get Import Job] operation of [CCD Definition Store]

     Then a negative response is received
      And the response has all other details as expected

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
@S-113.4
Scenario: must return 409 when submitting an import with a duplicate job id

    Given a user with [an active profile in CCD]

     When a request is prepared with appropriate values
      And the request [contains a supplied X-Import-Job-Id request header]
      And it is submitted to call the [Import definition file] operation of [CCD Definition Store]

     Then a positive response is received
      And the response has all other details as expected
      And a call [to import again with the same job id] will get the expected response as in [S-113.4_DuplicatePost].

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
