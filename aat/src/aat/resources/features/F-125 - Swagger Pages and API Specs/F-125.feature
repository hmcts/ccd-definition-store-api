#===============================================
@F-125
Feature: F-125: Swagger Pages and Open API Specs
#===============================================

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
@S-125.1
Scenario: must show Swagger UI page APIs

    Given an appropriate test context as detailed in the test data source,

     When a request is prepared with appropriate values,
      And it is submitted to call the [Get Swagger UI Page] operation of [CCD Definition Store],

     Then a positive response is received,
      And the response has all the details as expected
      And a call [to observe the swagger UI content] will get the expected response as in [S-125.1].
#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
@S-125.2
Scenario: must show Swagger Json APIs

    Given an appropriate test context as detailed in the test data source,

     When a request is prepared with appropriate values,
      And it is submitted to call the [Get Swagger Json API] operation of [CCD Definition Store],

     Then a positive response is received,
      And the response has all the details as expected
      And a call [to observe the swagger json content] will get the expected response as in [S-125.2].

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
