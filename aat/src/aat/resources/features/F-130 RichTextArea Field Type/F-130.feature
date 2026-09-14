@F-130
Feature: F-130: 'RichTextArea' Base Field Type

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-130.1
  Scenario: Must return all details successfully for a case type containing RichTextArea fields
    Given a user with [an active profile in CCD],
    When a request is prepared with appropriate values,
    And the request [contains id of a case type with RichTextArea fields],
    And it is submitted to call the [Fetch a Case Type Schema] operation of [CCD Definition Store],
    Then a positive response is received,
    And the response [contains a case field whose type resolves to the RichTextArea base type],
    And the response [contains a case field whose Min constraint has been carried through to a derived RichTextArea type],
    And the response has all other details as expected.
