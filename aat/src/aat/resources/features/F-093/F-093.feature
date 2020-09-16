@F-093
Feature: F-New: 'Retain hidden field'

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-600.1 #AC1 on RDM-9024
  Scenario: must import successfully when definition file contains correctly configured 'retainHiddenValue' column
    Given a user with [permissions to import case definition files]
    When a request is prepared with appropriate values
    And the request [contains correctly configured 'retainHiddenValue' column, as per ACs on RDM-8200]
    And it is submitted to call the [POST Case Definition File] operation of [CCD Definition Store Api]
    Then a positive response is received
    And the response has all other details as expected

  @S-600.2 #AC2 on RDM-9024
  Scenario: Import must fail when definition file contains invalid configuration for 'retainHiddenValue' on a field that uses a showCondition
    Given a user with [permissions to import case definition files]
    When a request is prepared with appropriate values
    And the request [contains incorrectly configured 'retainHiddenValue' column, see notes below]
    And it is submitted to call the [POST Case Definition File] operation of [CCD Definition Store Api]
    Then a negative response is received
    And the response has all other details as expected
    And the response [provides a clear validation message describing why the definition import has failed, for example, retainHiddenValue has been incorrectly configured or is invalid for fieldID [FieldID] on [TabName]]

  @S-600.3 #AC3 on RDM-9024
  Scenario: Import must fail when definition file contains configuration for 'retainHiddenValue' on a field that does not use a showCondition
    Given a user with [permissions to import case definition files]
    When a request is prepared with appropriate values
    And the request [contains configuration on the 'retainHiddenValue' column for a field that does not contain a showCondition]
    And it is submitted to call the [POST Case Definition File] operation of [CCD Definition Store Api]
    Then a negative response is received
    And the response has all other details as expected
    And the response [provides a clear validation message describing why the definition import has failed, for example, retainHiddenValue  can only be configured for a field that uses a showCondition. Field [FieldID] on [TabName] does not use a showCondition]

  @S-600.4 #AC6 on RDM-8200
  Scenario: Import must fail when definition file contains configuration for 'retainHiddenValue' set to 'No' for a complex field but 'Yes' on some of its subfields
    Given a user with [permissions to import case definition files]
    When a request is prepared with appropriate values
    And the request [contains configuration for a 'complexField' configured with a show condition, and with some of its subfields configured with show conditions]
    And the request [contains configuration for a 'complexField' is configured with "retainHiddenValue" to "No" and some of those subfields are configured with "retainHiddenValue" to "Yes"]
    And it is submitted to call the [POST Case Definition File] operation of [CCD Definition Store Api]
    Then a negative response is received
    And the response has all other details as expected
    And the response [provides a clear validation message describing why the definition import has failed, for example, retainHiddenValue  can only be configured for a field that uses a showCondition. Field [FieldID] on [TabName] does not use a showCondition]
