@F-106
Feature: F-106 New CategoryId Column

  Background:
    Given an appropriate test context as detailed in the test data source,

  @S-106.1
  @AC-1
  Scenario: Import Definition file with blank values in the CategoryID column of the CaseField tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a blank CategoryID value]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-106.2
  @AC-2
  Scenario: Import Definition file without the CategoryID column in the CaseField tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a missing CategoryID value column]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-106.3
  @AC-3 @AC-4 @AC-7 @AC-8
  @AC-15 @AC-16 @AC-19 @AC-20
  Scenario: Import definition file with valid CategoryID column in CaseField and ComplexType tabs – should return 200 response
    Given a user with [an active profile in CCD]
    And a call [to import definition file] will get the expected response as in [Import_BEFTA_Master_Definition]
    When a request is prepared with appropriate values
    And it is submitted to call the [Fetch a Case Type Schema] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected
    And the response [contains filled in CategoryID for Document value]
    And the response [contains filled in CategoryID for Document Collection value]
    And the response [contains null CategoryID for Document value]
    And the response [contains null CategoryID for Document Collection value]
    And the response [contains filled in CategoryID for Document ComplexType value],
    And the response [contains filled in CategoryID for Document Collection ComplexType value],
    And the response [contains null CategoryID for Document ComplexType value],
    And the response [contains null CategoryID for Document Collection ComplexType value]

  @S-106.4
  @AC-5
  Scenario: Same as AC3 but the CategoryID does not exist in the Categories tab – should return 400 when operation is submitted.
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a missing CategoryID in the Categories tab being referenced as a Document value in the CaseField tab]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-106.5
  @AC-6
  Scenario: Same as AC4, but CategoryID does not exist in the Categories tab – should return 400 when operation is submitted.
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a missing CategoryID in the Categories tab being referenced as a Document Collection value in the CaseField tab]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-106.6
  @AC-9
  Scenario: Import definition file with valid CategoryID column, but for field type other than Document type in CaseField tab – should return 400 response
            (Same as AC3 but CategoryID is defined for a field type Text)
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a CategoryID in the Categories tab being referenced as a Text value in the CaseField tab]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-106.7
  @AC-10
  Scenario: Import definition file with valid CategoryID column, but for field type other than Document type in CaseField tab – should return 400 response
            (Same as AC3 but CategoryID is defined for a field type TextArea)
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a CategoryID in the Categories tab being referenced as a TextArea value in the CaseField tab]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-106.8
  @AC-11
  Scenario: Import definition file with valid CategoryID column, but for field type other than Document type in CaseField tab – should return 400 response
            (Same as AC3 but CategoryID is defined for a field type Collection of Text)
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a CategoryID in the Categories tab being referenced as a Collection of type Text value in the CaseField tab]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-106.9
  @AC-12
  Scenario: Import definition file with valid CategoryID column, but for field type other than Document type in CaseField tab – should return 400 response
            (Same as AC3 but CategoryID is defined for a field type YesOrNo)
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a CategoryID in the Categories tab being referenced as a YesOrNo value in the CaseField tab]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-106.10
  @AC-13
  Scenario: Import Definition file with blank values in the CategoryID column of the ComplexTypes tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a blank CategoryID value in the ComplexTypes tab]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-106.11
  @AC-14
  Scenario: Import Definition file without the CategoryID column in the ComplexTypes tab
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a missing CategoryID in the ComplexTypes tab]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a positive response is received
    And the response has all other details as expected

  @S-106.12
  @AC-17
  Scenario: Same as AC15, but CategoryID does not exist in the Categories tab – should return 400 when operation is submitted.
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a missing CategoryID in the Categories tab being referenced as a Document value in the ComplexTypes tab]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-106.13
  @AC-18
  Scenario: Same as AC16, but CategoryID does not exist in the Categories tab – should return 400 when operation is submitted.
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a missing CategoryID in the Categories tab being referenced as a Document Collection value in the ComplexTypes tab]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-106.14
  @AC-21
  Scenario: Import definition file with valid CategoryID column, but for field type other than Document type in ComplexTypes tab – should return 400 response
  (Same as AC3 but CategoryID is defined for a field type Text)
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a CategoryID in the Categories tab being referenced as a Text value in the ComplexTypes tab]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-106.15
  @AC-22
  Scenario: Import definition file with valid CategoryID column, but for field type other than Document type in ComplexTypes tab – should return 400 response
  (Same as AC3 but CategoryID is defined for a field type TextArea)
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a CategoryID in the Categories tab being referenced as a TextArea value in the ComplexTypes tab]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-106.16
  @AC-23
  Scenario: Import definition file with valid CategoryID column, but for field type other than Document type in ComplexTypes tab – should return 400 response
  (Same as AC3 but CategoryID is defined for a field type Collection of Text)
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a CategoryID in the Categories tab being referenced as a Collection of type Text value in the ComplexTypes tab]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected

  @S-106.17
  @AC-24
  Scenario: Import definition file with valid CategoryID column, but for field type other than Document type in ComplexTypes tab – should return 400 response
  (Same as AC3 but CategoryID is defined for a field type YesOrNo)
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a CategoryID in the Categories tab being referenced as a YesOrNo value in the ComplexTypes tab]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    Then a negative response is received
    And the response has all other details as expected
