@F-108
Feature: F-108: Import validations for the ComplexTypes should fail when overriding base types

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-108.1 #AC1
  Scenario: Must return a negative response in an attempt to import a definition file when ComplexTypes contains field with ID 'OrderSummary'
    Given a user with [permissions to import case definition files]
    When a request is prepared with appropriate values
    And the request [contains complex field type OrderSummary defined]
    And it is submitted to call the [POST Case Definition File] operation of [CCD Definition Store Api]
    Then a negative response is received
    And the response has all other details as expected
    And the response [provides a clear validation message describing why the definition import has failed, for example, 'OrderSummary' complex type defined in worksheet [ComplexTypes] tries to override a base complex 'OrderSummary' type]

  @S-108.2 #AC2
   Scenario: Must return a negative response in an attempt to import a definition file when ComplexTypes contains field with ID 'Text'
     Given a user with [permissions to import case definition files]
     When a request is prepared with appropriate values
     And the request [contains complex field type Text defined]
     And it is submitted to call the [POST Case Definition File] operation of [CCD Definition Store Api]
     Then a negative response is received
     And the response has all other details as expected
     And the response [provides a clear validation message describing why the definition import has failed, for example, 'Text' complex type defined in worksheet [ComplexTypes] tries to override a base 'Text' type]


