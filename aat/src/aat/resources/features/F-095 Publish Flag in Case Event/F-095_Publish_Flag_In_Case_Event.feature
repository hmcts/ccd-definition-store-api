@F-095
Feature: F-095: Publish Events column

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-095.1  # ACA-1
  Scenario: Must successfully import a definition file containing a valid value in Publish Events column
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a valid value configured in Publish Events column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected
    And a call [fetch a Case Type Schema] will get the expected response as in [S-095.1_Get_CaseType].

  @S-095.2 # ACA-2
  Scenario: Must return a negative response in an attempt to import a definition file containing a invalid value Publish Events column
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a invalid value configured in Publish Events column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-095.3  # ACA-3
  Scenario: Must successfully import a definition file containing a blank value in Publish Events column
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a blank value in Publish Events column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected
    And a call [fetch a Case Type Schema] will get the expected response as in [S-095.3_Get_CaseType].

  @S-095.4
  Scenario: Must return a negative response in an attempt to import a definition file containing invalid value in Publish Events column of CaseEventToFields
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains invalid value in Publish Events column of CaseEventToFields]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-095.5
  Scenario: Must return a negative response in an attempt to import a definition file containing invalid value in Publish Events column of CaseEventToComplexType
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains invalid value in Publish Events column of CaseEventToComplexType]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-095.6
  Scenario: Must return a negative response in an attempt to import a definition file where Publish column is set to No in CaseEvent but set to Yes in CaseEventToFields
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains No in CaseEvent Publish column and Yes in CaseEventToFields Publish column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-095.7
  Scenario: Must return a negative response in an attempt to import a definition file where Publish column is set to No in CaseEvent but set to Yes in CaseEventToComplexType
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains No in CaseEvent Publish column and Yes in CaseEventToComplexType Publish column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-095.8
  Scenario: Must successfully import a definition file where Publish column is missing
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains missing Publish column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected
    And a call [fetch a Case Type Schema] will get the expected response as in [S-095.8_Get_CaseType].

  @S-095.9
  Scenario: Must return a negative response in an attempt to import a definition file containing invalid value in PublishAs column of CaseEventToFields
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains invalid value in PublishAs column of CaseEventToFields]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-095.10
  Scenario: Must return a negative response in an attempt to import a definition file containing invalid value in PublishAs column of CaseEventToComplexType
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains invalid value in PublishAs column of CaseEventToComplexType]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-095.11
  Scenario: Must return a negative response in an attempt to import a definition file where PublishAs value is not unique within the same event and case type across CaseEventToFields and CaseEventToComplexType  
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a non-unique PublishAs value within the same event and case type]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-095.12
  Scenario: Must successfully import a definition file where two different events have the same PublishAs value for fields in the same CaseType
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains two different events with the same PublishAs value for fields in the same CaseType]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected
    And a call [fetch a Case Type Schema] will get the expected response as in [S-095.12_Get_CaseType].


