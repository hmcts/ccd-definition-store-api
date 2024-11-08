@F-112
Feature: F-112: New NullifyByDefault Column

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-112.1
  Scenario: Import definition file with new field NullifyByDefault set to Yes
    Given a user with [an active profile in CCD and with CCD-import role]
    When a request is prepared with appropriate values
    And the request [contains a definition file that contains NullifyByDefault new field and NullifyByDefault is set to Yes]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    And the response [has the 200 OK code]
    Then a positive response is received
    And the response has all other details as expected

  @S-112.2
  Scenario: Import definition file with new field NullifyByDefault set to No
    Given a user with [an active profile in CCD and with CCD-import role]
    When a request is prepared with appropriate values
    And the request [contains a definition file that contains NullifyByDefault new field and NullifyByDefault is set to No]
    And the request [contains a definition file that contains DefaultValue field has a value set]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    And the response [has the 200 OK code]
    Then a positive response is received
    And the response has all other details as expected

  @S-112.3
  Scenario: Return error when DefaultValue has a value and NullifyByDefault is set to Yes
    Given a user with [an active profile in CCD and with CCD-import role]
    When a request is prepared with appropriate values
    And the request [contains a definition file that contains NullifyByDefault new field and NullifyByDefault is set to Yes]
    And the request [contains a definition file that contains DefaultValue field has a value set]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    And the response [has the 400 Bad Request code]
    Then a negative response is received

  @S-112.4
  Scenario: Return success when DefaultValue has a value and NullifyByDefault is set to No
    Given a user with [an active profile in CCD and with CCD-import role]
    When a request is prepared with appropriate values
    And the request [contains a definition file that contains NullifyByDefault new field and NullifyByDefault is set to No]
    And the request [contains a definition file that contains DefaultValue field has a value set]
    And it is submitted to call the [Import definition file] operation of [CCD Definition Store]
    And the response [has the 200 OK code]
    Then a positive response is received
    And the response has all other details as expected

  @S-112.5
  Scenario: Retrieve CaseEventToFields values
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a valid jurisdiction id]
    And the request [contains a valid caseType id]
    And it is submitted to call the [Get Case Type Details] operation of [CCD Definition Store]
    Then the response [has the 200 OK code]
    And the response [contains case type details]
