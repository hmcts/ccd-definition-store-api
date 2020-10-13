#====================================
@F-000
Feature: F-000: Healthcheck Operation
#====================================

Background:
    Given an appropriate test context as detailed in the test data source

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
@S-000
Scenario: must return a successful response from the Healthcheck Operation

     When a request is prepared with appropriate values
      And it is submitted to call the [Healthcheck] operation of [CCD Definition Store]

     Then a positive response is received
      And the response [has the 200 OK code]
      And the response has all other details as expected.

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

