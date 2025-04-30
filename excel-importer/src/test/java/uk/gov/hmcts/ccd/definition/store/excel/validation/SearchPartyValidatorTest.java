package uk.gov.hmcts.ccd.definition.store.excel.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchPartyEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.definition.store.excel.validation.SearchPartyValidator.COMMA_SEPARATOR;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_COLLECTION;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_COMPLEX;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_TEXT;

@DisplayName("SearchPartyValidator")
@ExtendWith(MockitoExtension.class)
class SearchPartyValidatorTest {

    private static final String CASE_TYPE = "TestCaseType";

    private static final String TEST_EXPRESSION_NAME_SINGLE_1 = "Name1";
    private static final String TEST_EXPRESSION_NAME_SINGLE_2 = "Name2";
    private static final String TEST_EXPRESSION_NAME_MULTIPLE =
        TEST_EXPRESSION_NAME_SINGLE_1 + COMMA_SEPARATOR + TEST_EXPRESSION_NAME_SINGLE_2;

    private static final String TEST_EXPRESSION_EMAIL_1 = "Email1";
    private static final String TEST_EXPRESSION_ADDRESS_1 = "Address1";
    private static final String TEST_EXPRESSION_POSTCODE_1 = "Postcode1";
    private static final String TEST_EXPRESSION_DOB_1 = "DOB1";
    private static final String TEST_EXPRESSION_DOD_1 = "DOD1";

    private static final String TEST_EXPRESSION_COLLECTION_FIELD_NAME_EMPTY = "";
    private static final String TEST_EXPRESSION_COLLECTION_FIELD_NAME_1 = "Name1";
    private static final String TEST_EXPRESSION_COLLECTION_FIELD_NAME_2 = "Name2";

    private static final String TEST_EXPRESSION_BAD = "BadTestExpression";

    @Mock
    private DotNotationValidator dotNotationValidator;

    @Mock
    private ParseContext parseContext;

    private FieldTypeEntity complexTypeUsedByCollection;

    private FieldTypeEntity collectionFieldNameFieldType;

    @InjectMocks
    private SearchPartyValidator searchPartyValidator;

    @BeforeEach
    public void setUp() {
        setUpCollectionFieldNameFieldType();
    }

    @DisplayName("should validate all fields without collection field")
    @Test
    void shouldValidateAllFieldsWithoutCollectionField() {

        // GIVEN
        stubValidateWithoutCollection();

        SearchPartyEntity searchPartyEntity1 = createPopulatedSearchPartyEntityWithoutCollectionField();

        // WHEN
        searchPartyValidator.validate(List.of(searchPartyEntity1), parseContext);

        // THEN
        verifyDotNotationValidatorCallMadeFor(ColumnName.SEARCH_PARTY_NAME,
                                              searchPartyEntity1.getSearchPartyName());
        verifyDotNotationValidatorCallMadeFor(ColumnName.SEARCH_PARTY_EMAIL_ADDRESS,
                                              searchPartyEntity1.getSearchPartyEmailAddress());
        verifyDotNotationValidatorCallMadeFor(ColumnName.SEARCH_PARTY_ADDRESS_LINE_1,
                                              searchPartyEntity1.getSearchPartyAddressLine1());
        verifyDotNotationValidatorCallMadeFor(ColumnName.SEARCH_PARTY_POST_CODE,
                                              searchPartyEntity1.getSearchPartyPostCode());
        verifyDotNotationValidatorCallMadeFor(ColumnName.SEARCH_PARTY_DOB,
                                              searchPartyEntity1.getSearchPartyDob());
        verifyDotNotationValidatorCallMadeFor(ColumnName.SEARCH_PARTY_DOD,
                                              searchPartyEntity1.getSearchPartyDod());

        verifyDotNotationValidatorCallNeverMadeFor(ColumnName.SEARCH_PARTY_COLLECTION_FIELD_NAME);
    }

    @DisplayName("should validate all fields with collection field")
    @Test
    void shouldValidateAllFieldsWithCollectionField() {

        // GIVEN
        stubValidateWithCollection();

        SearchPartyEntity collectionSearchPartyEntity1 = createPopulatedSearchPartyEntityWithCollectionField();

        // WHEN
        searchPartyValidator.validate(List.of(collectionSearchPartyEntity1), parseContext);

        // THEN
        verifyDotNotationValidatorWithComplexTypeCallMadeFor(ColumnName.SEARCH_PARTY_NAME,
                                                             collectionSearchPartyEntity1.getSearchPartyName());
        verifyDotNotationValidatorWithComplexTypeCallMadeFor(ColumnName.SEARCH_PARTY_EMAIL_ADDRESS,
                                                             collectionSearchPartyEntity1.getSearchPartyEmailAddress());
        verifyDotNotationValidatorWithComplexTypeCallMadeFor(ColumnName.SEARCH_PARTY_ADDRESS_LINE_1,
                                                             collectionSearchPartyEntity1.getSearchPartyAddressLine1());
        verifyDotNotationValidatorWithComplexTypeCallMadeFor(ColumnName.SEARCH_PARTY_POST_CODE,
                                                             collectionSearchPartyEntity1.getSearchPartyPostCode());
        verifyDotNotationValidatorWithComplexTypeCallMadeFor(ColumnName.SEARCH_PARTY_DOB,
                                                             collectionSearchPartyEntity1.getSearchPartyDob());
        verifyDotNotationValidatorWithComplexTypeCallMadeFor(ColumnName.SEARCH_PARTY_DOD,
                                                             collectionSearchPartyEntity1.getSearchPartyDod());

        verifyDotNotationValidateCallMadeForCollectionFieldName(
            collectionSearchPartyEntity1.getSearchPartyCollectionFieldName());
    }

    @DisplayName("should validate multiple searchPartyEntity values")
    @Test
    void shouldValidateMultipleSearchPartyEntities() {

        // GIVEN
        stubValidateWithCollection();
        stubValidateWithoutCollection();

        SearchPartyEntity searchPartyEntity1 = createBlankSearchPartyEntity();
        searchPartyEntity1.setSearchPartyName(TEST_EXPRESSION_NAME_SINGLE_1);

        SearchPartyEntity searchPartyEntity2 = createBlankSearchPartyEntity();
        searchPartyEntity2.setSearchPartyName(TEST_EXPRESSION_NAME_SINGLE_2);

        SearchPartyEntity collectionSearchPartyEntity1 = createBlankSearchPartyEntity();
        collectionSearchPartyEntity1.setSearchPartyCollectionFieldName(TEST_EXPRESSION_COLLECTION_FIELD_NAME_1);
        collectionSearchPartyEntity1.setSearchPartyName(TEST_EXPRESSION_NAME_SINGLE_1);

        SearchPartyEntity collectionSearchPartyEntity2 = createBlankSearchPartyEntity();
        collectionSearchPartyEntity2.setSearchPartyCollectionFieldName(TEST_EXPRESSION_COLLECTION_FIELD_NAME_2);
        collectionSearchPartyEntity2.setSearchPartyName(TEST_EXPRESSION_NAME_SINGLE_2);

        // WHEN
        searchPartyValidator.validate(
            List.of(searchPartyEntity1, searchPartyEntity2, collectionSearchPartyEntity1, collectionSearchPartyEntity2),
            parseContext
        );

        // THEN
        verifyDotNotationValidatorCallMadeFor(ColumnName.SEARCH_PARTY_NAME, TEST_EXPRESSION_NAME_SINGLE_1);
        verifyDotNotationValidatorCallMadeFor(ColumnName.SEARCH_PARTY_NAME, TEST_EXPRESSION_NAME_SINGLE_2);

        verifyDotNotationValidatorWithComplexTypeCallMadeFor(
            ColumnName.SEARCH_PARTY_NAME, TEST_EXPRESSION_NAME_SINGLE_1);
        verifyDotNotationValidatorWithComplexTypeCallMadeFor(
            ColumnName.SEARCH_PARTY_NAME, TEST_EXPRESSION_NAME_SINGLE_2);
    }

    @DisplayName("should validate SearchPartyName when supplied as CSV")
    @Test
    void shouldValidateSearchPartyNameCsv() {

        // GIVEN
        stubValidateWithCollection();
        stubValidateWithoutCollection();

        SearchPartyEntity searchPartyEntity1 = createBlankSearchPartyEntity();
        searchPartyEntity1.setSearchPartyName(TEST_EXPRESSION_NAME_MULTIPLE);

        SearchPartyEntity collectionSearchPartyEntity1 = createBlankSearchPartyEntity();
        collectionSearchPartyEntity1.setSearchPartyCollectionFieldName(TEST_EXPRESSION_COLLECTION_FIELD_NAME_1);
        collectionSearchPartyEntity1.setSearchPartyName(TEST_EXPRESSION_NAME_MULTIPLE);

        // WHEN
        searchPartyValidator.validate(List.of(searchPartyEntity1, collectionSearchPartyEntity1), parseContext);

        // THEN
        verifyDotNotationValidatorCallMadeFor(ColumnName.SEARCH_PARTY_NAME, TEST_EXPRESSION_NAME_SINGLE_1);
        verifyDotNotationValidatorCallMadeFor(ColumnName.SEARCH_PARTY_NAME, TEST_EXPRESSION_NAME_SINGLE_2);

        verifyDotNotationValidatorWithComplexTypeCallMadeFor(
            ColumnName.SEARCH_PARTY_NAME, TEST_EXPRESSION_NAME_SINGLE_1);
        verifyDotNotationValidatorWithComplexTypeCallMadeFor(
            ColumnName.SEARCH_PARTY_NAME, TEST_EXPRESSION_NAME_SINGLE_2);
    }

    @ParameterizedTest(name = "should validate as ok even if SearchPartyName is blank: {0}")
    @NullAndEmptySource
    void shouldValidateAsOkEvenIfSearchPartyNameIsBlank(String value) {

        // GIVEN
        stubValidateWithCollection();
        stubValidateWithoutCollection();

        SearchPartyEntity searchPartyEntity1 = createPopulatedSearchPartyEntityWithoutCollectionField();
        searchPartyEntity1.setSearchPartyName(value);

        SearchPartyEntity collectionSearchPartyEntity1 = createPopulatedSearchPartyEntityWithCollectionField();
        collectionSearchPartyEntity1.setSearchPartyName(value);

        // WHEN
        searchPartyValidator.validate(List.of(searchPartyEntity1, collectionSearchPartyEntity1), parseContext);

        // THEN
        verifyDotNotationValidatorCallMadeAtLeastOnce(); // verify at least one validation ...
        verifyDotNotationValidatorWithComplexTypeCallMadeAtLeastOnce(); // ... and one collection validation ...
        verifyDotNotationValidatorCallNeverMadeFor(ColumnName.SEARCH_PARTY_NAME); // ... but none for blank field
    }

    @ParameterizedTest(name = "should validate as ok even if SearchPartyEmailAddress is blank: {0}")
    @NullAndEmptySource
    void shouldValidateAsOkEvenIfSearchPartyEmailAddressIsBlank(String value) {

        // GIVEN
        stubValidateWithCollection();
        stubValidateWithoutCollection();

        SearchPartyEntity searchPartyEntity1 = createPopulatedSearchPartyEntityWithoutCollectionField();
        searchPartyEntity1.setSearchPartyEmailAddress(value);

        SearchPartyEntity collectionSearchPartyEntity1 = createPopulatedSearchPartyEntityWithCollectionField();
        collectionSearchPartyEntity1.setSearchPartyEmailAddress(value);

        // WHEN
        searchPartyValidator.validate(List.of(searchPartyEntity1, collectionSearchPartyEntity1), parseContext);

        // THEN
        verifyDotNotationValidatorCallMadeAtLeastOnce(); // verify at least one validation ...
        verifyDotNotationValidatorWithComplexTypeCallMadeAtLeastOnce(); // ... and one collection validation ...
        verifyDotNotationValidatorCallNeverMadeFor(ColumnName.SEARCH_PARTY_EMAIL_ADDRESS); //...but none for blank field
    }

    @ParameterizedTest(name = "should validate as ok even if SearchPartyAddressLine1 is blank: {0}")
    @NullAndEmptySource
    void shouldValidateAsOkEvenIfSearchPartyAddressLine1IsBlank(String value) {

        // GIVEN
        stubValidateWithCollection();
        stubValidateWithoutCollection();

        SearchPartyEntity searchPartyEntity1 = createPopulatedSearchPartyEntityWithoutCollectionField();
        searchPartyEntity1.setSearchPartyAddressLine1(value);

        SearchPartyEntity collectionSearchPartyEntity1 = createPopulatedSearchPartyEntityWithCollectionField();
        collectionSearchPartyEntity1.setSearchPartyAddressLine1(value);

        // WHEN
        searchPartyValidator.validate(List.of(searchPartyEntity1, collectionSearchPartyEntity1), parseContext);

        // THEN
        verifyDotNotationValidatorCallMadeAtLeastOnce(); // verify at least one validation ...
        verifyDotNotationValidatorWithComplexTypeCallMadeAtLeastOnce(); // ... and one collection validation ...
        verifyDotNotationValidatorCallNeverMadeFor(ColumnName.SEARCH_PARTY_ADDRESS_LINE_1);//...but none for blank field
    }

    @ParameterizedTest(name = "should validate as ok even if SearchPartyPostCode is blank: {0}")
    @NullAndEmptySource
    void shouldValidateAsOkEvenIfSearchPartyPostCodeIsBlank(String value) {

        // GIVEN
        stubValidateWithCollection();
        stubValidateWithoutCollection();

        // GIVEN
        SearchPartyEntity searchPartyEntity1 = createPopulatedSearchPartyEntityWithoutCollectionField();
        searchPartyEntity1.setSearchPartyPostCode(value);

        SearchPartyEntity collectionSearchPartyEntity1 = createPopulatedSearchPartyEntityWithCollectionField();
        collectionSearchPartyEntity1.setSearchPartyPostCode(value);

        // WHEN
        searchPartyValidator.validate(List.of(searchPartyEntity1, collectionSearchPartyEntity1), parseContext);

        // THEN
        verifyDotNotationValidatorCallMadeAtLeastOnce(); // verify at least one validation ...
        verifyDotNotationValidatorWithComplexTypeCallMadeAtLeastOnce(); // ... and one collection validation ...
        verifyDotNotationValidatorCallNeverMadeFor(ColumnName.SEARCH_PARTY_POST_CODE); // ... but none for blank field
    }

    @ParameterizedTest(name = "should validate as ok even if SearchPartyDob is blank: {0}")
    @NullAndEmptySource
    void shouldValidateAsOkEvenIfSearchPartyDobIsBlank(String value) {

        // GIVEN
        stubValidateWithCollection();
        stubValidateWithoutCollection();

        SearchPartyEntity searchPartyEntity1 = createPopulatedSearchPartyEntityWithoutCollectionField();
        searchPartyEntity1.setSearchPartyDob(value);

        SearchPartyEntity collectionSearchPartyEntity1 = createPopulatedSearchPartyEntityWithCollectionField();
        collectionSearchPartyEntity1.setSearchPartyDob(value);

        // WHEN
        searchPartyValidator.validate(List.of(searchPartyEntity1, collectionSearchPartyEntity1), parseContext);

        // THEN
        verifyDotNotationValidatorCallMadeAtLeastOnce(); // verify at least one validation ...
        verifyDotNotationValidatorWithComplexTypeCallMadeAtLeastOnce(); // ... and one collection validation ...
        verifyDotNotationValidatorCallNeverMadeFor(ColumnName.SEARCH_PARTY_DOB); // ... but none for blank field
    }

    @ParameterizedTest(name = "should validate as ok even if SearchPartyDob is blank: {0}")
    @NullAndEmptySource
    void shouldValidateAsOkEvenIfSearchPartyDodIsBlank(String value) {

        // GIVEN
        stubValidateWithCollection();
        stubValidateWithoutCollection();

        SearchPartyEntity searchPartyEntity1 = createPopulatedSearchPartyEntityWithoutCollectionField();
        searchPartyEntity1.setSearchPartyDod(value);

        SearchPartyEntity collectionSearchPartyEntity1 = createPopulatedSearchPartyEntityWithCollectionField();
        collectionSearchPartyEntity1.setSearchPartyDod(value);

        // WHEN
        searchPartyValidator.validate(List.of(searchPartyEntity1, collectionSearchPartyEntity1), parseContext);

        // THEN
        verifyDotNotationValidatorCallMadeAtLeastOnce(); // verify at least one validation ...
        verifyDotNotationValidatorWithComplexTypeCallMadeAtLeastOnce(); // ... and one collection validation ...
        verifyDotNotationValidatorCallNeverMadeFor(ColumnName.SEARCH_PARTY_DOD); // ... but none for blank field
    }

    @DisplayName("Exception tests (without collection)")
    @Nested
    class ExceptionTestsWithoutCollection {

        @BeforeEach
        void setup() {
            // default stubbing must be explicitly set to prevent PotentialStubbingProblem when testing throws
            stubValidateWithoutCollection();
        }

        @DisplayName("throws exception if SearchPartyName validation fails")
        @Test
        void throwsExceptionIfSearchPartyNameValidationFails() {

            // GIVEN
            SearchPartyEntity searchPartyEntity1 = createPopulatedSearchPartyEntityWithoutCollectionField();
            searchPartyEntity1.setSearchPartyName(
                // NB: 1 good expression and 1 bad expression
                TEST_EXPRESSION_NAME_SINGLE_1 + COMMA_SEPARATOR + TEST_EXPRESSION_BAD
            );
            Exception expectedException = prepareMockDotNotationValidatorToThrow(ColumnName.SEARCH_PARTY_NAME);

            // WHEN & THEN
            Exception actualException = assertThrows(expectedException.getClass(), () ->
                searchPartyValidator.validate(List.of(searchPartyEntity1), parseContext)
            );

            assertEquals(expectedException, actualException);
        }

        @DisplayName("throws exception if SearchPartyEmailAddress validation fails")
        @Test
        void throwsExceptionIfSearchPartyEmailAddressValidationFails() {

            // GIVEN
            SearchPartyEntity searchPartyEntity1 = createPopulatedSearchPartyEntityWithoutCollectionField();
            searchPartyEntity1.setSearchPartyEmailAddress(TEST_EXPRESSION_BAD);
            Exception expectedException = prepareMockDotNotationValidatorToThrow(ColumnName.SEARCH_PARTY_EMAIL_ADDRESS);

            // WHEN & THEN
            Exception actualException = assertThrows(expectedException.getClass(), () ->
                searchPartyValidator.validate(List.of(searchPartyEntity1), parseContext)
            );

            assertEquals(expectedException, actualException);
        }

        @DisplayName("throws exception if SearchPartyAddressLine1 validation fails")
        @Test
        void throwsExceptionIfSearchPartyAddressLine1ValidationFails() {

            // GIVEN
            SearchPartyEntity searchPartyEntity1 = createPopulatedSearchPartyEntityWithoutCollectionField();
            searchPartyEntity1.setSearchPartyAddressLine1(TEST_EXPRESSION_BAD);
            Exception expectedException
                = prepareMockDotNotationValidatorToThrow(ColumnName.SEARCH_PARTY_ADDRESS_LINE_1);

            // WHEN & THEN
            Exception actualException = assertThrows(expectedException.getClass(), () ->
                searchPartyValidator.validate(List.of(searchPartyEntity1), parseContext)
            );

            assertEquals(expectedException, actualException);
        }

        @DisplayName("throws exception if SearchPartyPostCode validation fails")
        @Test
        void throwsExceptionIfSearchPartyPostCodeValidationFails() {

            // GIVEN
            SearchPartyEntity searchPartyEntity1 = createPopulatedSearchPartyEntityWithoutCollectionField();
            searchPartyEntity1.setSearchPartyPostCode(TEST_EXPRESSION_BAD);
            Exception expectedException = prepareMockDotNotationValidatorToThrow(ColumnName.SEARCH_PARTY_POST_CODE);

            // WHEN & THEN
            Exception actualException = assertThrows(expectedException.getClass(), () ->
                searchPartyValidator.validate(List.of(searchPartyEntity1), parseContext)
            );

            assertEquals(expectedException, actualException);
        }

        @DisplayName("throws exception if SearchPartyDob validation fails")
        @Test
        void throwsExceptionIfSearchPartyDobValidationFails() {

            // GIVEN
            SearchPartyEntity searchPartyEntity1 = createPopulatedSearchPartyEntityWithoutCollectionField();
            searchPartyEntity1.setSearchPartyDob(TEST_EXPRESSION_BAD);
            Exception expectedException = prepareMockDotNotationValidatorToThrow(ColumnName.SEARCH_PARTY_DOB);

            // WHEN & THEN
            Exception actualException = assertThrows(expectedException.getClass(), () ->
                searchPartyValidator.validate(List.of(searchPartyEntity1), parseContext)
            );

            assertEquals(expectedException, actualException);
        }

        @DisplayName("throws exception if SearchPartyDod validation fails")
        @Test
        void throwsExceptionIfSearchPartyDodValidationFails() {

            // GIVEN
            SearchPartyEntity searchPartyEntity1 = createPopulatedSearchPartyEntityWithoutCollectionField();
            searchPartyEntity1.setSearchPartyDod(TEST_EXPRESSION_BAD);
            Exception expectedException = prepareMockDotNotationValidatorToThrow(ColumnName.SEARCH_PARTY_DOD);

            // WHEN & THEN
            Exception actualException = assertThrows(expectedException.getClass(), () ->
                searchPartyValidator.validate(List.of(searchPartyEntity1), parseContext)
            );

            assertEquals(expectedException, actualException);
        }

        private Exception prepareMockDotNotationValidatorToThrow(ColumnName columnName) {
            InvalidImportException exception = new InvalidImportException();
            doThrow(exception).when(dotNotationValidator).validate(
                parseContext,
                SheetName.SEARCH_PARTY,
                columnName,
                CASE_TYPE,
                TEST_EXPRESSION_BAD
            );

            return exception;
        }
    }

    @DisplayName("Exception tests (with collection)")
    @Nested
    class ExceptionTestsWithCollection {

        @BeforeEach
        void setup() {
            // default stubbing must be explicitly set to prevent PotentialStubbingProblem when testing throws
            stubValidateWithCollection();
        }

        @DisplayName("throws exception if SearchPartyName validation fails")
        @Test
        void throwsExceptionIfSearchPartyNameValidationFails() {

            // GIVEN
            SearchPartyEntity collectionSearchPartyEntity1 = createPopulatedSearchPartyEntityWithCollectionField();
            collectionSearchPartyEntity1.setSearchPartyName(
                // NB: 1 good expression and 1 bad expression
                TEST_EXPRESSION_NAME_SINGLE_1 + COMMA_SEPARATOR + TEST_EXPRESSION_BAD
            );
            Exception expectedException = prepareMockDotNotationValidatorToThrow(ColumnName.SEARCH_PARTY_NAME);

            // WHEN & THEN
            Exception actualException = assertThrows(expectedException.getClass(), () ->
                searchPartyValidator.validate(List.of(collectionSearchPartyEntity1), parseContext)
            );

            assertEquals(expectedException, actualException);
        }

        @DisplayName("throws exception if SearchPartyEmailAddress validation fails")
        @Test
        void throwsExceptionIfSearchPartyEmailAddressValidationFails() {

            // GIVEN
            SearchPartyEntity collectionSearchPartyEntity1 = createPopulatedSearchPartyEntityWithCollectionField();
            collectionSearchPartyEntity1.setSearchPartyEmailAddress(TEST_EXPRESSION_BAD);
            Exception expectedException = prepareMockDotNotationValidatorToThrow(ColumnName.SEARCH_PARTY_EMAIL_ADDRESS);

            // WHEN & THEN
            Exception actualException = assertThrows(expectedException.getClass(), () ->
                searchPartyValidator.validate(List.of(collectionSearchPartyEntity1), parseContext)
            );

            assertEquals(expectedException, actualException);
        }

        @DisplayName("throws exception if SearchPartyAddressLine1 validation fails")
        @Test
        void throwsExceptionIfSearchPartyAddressLine1ValidationFails() {

            // GIVEN
            SearchPartyEntity collectionSearchPartyEntity1 = createPopulatedSearchPartyEntityWithCollectionField();
            collectionSearchPartyEntity1.setSearchPartyAddressLine1(TEST_EXPRESSION_BAD);
            Exception expectedException
                = prepareMockDotNotationValidatorToThrow(ColumnName.SEARCH_PARTY_ADDRESS_LINE_1);

            // WHEN & THEN
            Exception actualException = assertThrows(expectedException.getClass(), () ->
                searchPartyValidator.validate(List.of(collectionSearchPartyEntity1), parseContext)
            );

            assertEquals(expectedException, actualException);
        }

        @DisplayName("throws exception if SearchPartyPostCode validation fails")
        @Test
        void throwsExceptionIfSearchPartyPostCodeValidationFails() {

            // GIVEN
            SearchPartyEntity searchPartyEntity1 = createPopulatedSearchPartyEntityWithCollectionField();
            searchPartyEntity1.setSearchPartyPostCode(TEST_EXPRESSION_BAD);
            Exception expectedException = prepareMockDotNotationValidatorToThrow(ColumnName.SEARCH_PARTY_POST_CODE);

            // WHEN & THEN
            Exception actualException = assertThrows(expectedException.getClass(), () ->
                searchPartyValidator.validate(List.of(searchPartyEntity1), parseContext)
            );

            assertEquals(expectedException, actualException);
        }

        @DisplayName("throws exception if SearchPartyDob validation fails")
        @Test
        void throwsExceptionIfSearchPartyDobValidationFails() {

            // GIVEN
            SearchPartyEntity collectionSearchPartyEntity1 = createPopulatedSearchPartyEntityWithCollectionField();
            collectionSearchPartyEntity1.setSearchPartyDob(TEST_EXPRESSION_BAD);
            Exception expectedException = prepareMockDotNotationValidatorToThrow(ColumnName.SEARCH_PARTY_DOB);

            // WHEN & THEN
            Exception actualException = assertThrows(expectedException.getClass(), () ->
                searchPartyValidator.validate(List.of(collectionSearchPartyEntity1), parseContext)
            );

            assertEquals(expectedException, actualException);
        }

        @DisplayName("throws exception if SearchPartyDod validation fails")
        @Test
        void throwsExceptionIfSearchPartyDodValidationFails() {

            // GIVEN
            SearchPartyEntity searchPartyEntity1 = createPopulatedSearchPartyEntityWithCollectionField();
            searchPartyEntity1.setSearchPartyDod(TEST_EXPRESSION_BAD);
            Exception expectedException = prepareMockDotNotationValidatorToThrow(ColumnName.SEARCH_PARTY_DOD);

            // WHEN & THEN
            Exception actualException = assertThrows(expectedException.getClass(), () ->
                searchPartyValidator.validate(List.of(searchPartyEntity1), parseContext)
            );

            assertEquals(expectedException, actualException);
        }

        @DisplayName("throws exception if SearchPartyCollectionFieldName validation fails")
        @Test
        void throwsExceptionIfSearchPartyCollectionFieldNameValidationFails() {

            // GIVEN
            SearchPartyEntity collectionSearchPartyEntity1 = createPopulatedSearchPartyEntityWithCollectionField();
            collectionSearchPartyEntity1.setSearchPartyCollectionFieldName(TEST_EXPRESSION_BAD);
            Exception expectedException = prepareMockDotNotationValidatorToThrowForCollectionFieldName();

            // WHEN & THEN
            Exception actualException = assertThrows(expectedException.getClass(), () ->
                searchPartyValidator.validate(List.of(collectionSearchPartyEntity1), parseContext)
            );

            assertEquals(expectedException, actualException);
        }

        @DisplayName("throws exception if SearchPartyCollectionFieldName is not a collection")
        @Test
        void throwsExceptionIfSearchPartyCollectionFieldNameNotACollection() {

            // GIVEN
            SearchPartyEntity collectionSearchPartyEntity1 = createPopulatedSearchPartyEntityWithCollectionField();

            collectionFieldNameFieldType.setBaseFieldType(buildBaseTextType());
            List<SearchPartyEntity> searchPartyEntities = List.of(collectionSearchPartyEntity1);

            // WHEN/THEN
            assertThatExceptionOfType(InvalidImportException.class)
                .isThrownBy(() -> searchPartyValidator.validate(searchPartyEntities, parseContext))
                .withMessage(getExpectedCollectionTypeErrorMessage());
        }

        @DisplayName("throws exception if SearchPartyCollectionFieldName is not a collection of type complex")
        @Test
        void throwsExceptionIfSearchPartyCollectionFieldNameNotACollectionOfTypeComplex() {

            // GIVEN
            SearchPartyEntity collectionSearchPartyEntity1 = createPopulatedSearchPartyEntityWithCollectionField();

            collectionFieldNameFieldType.setCollectionFieldType(buildBaseTextType());
            List<SearchPartyEntity> searchPartyEntities = List.of(collectionSearchPartyEntity1);

            // WHEN/THEN
            assertThatExceptionOfType(InvalidImportException.class)
                .isThrownBy(() -> searchPartyValidator.validate(searchPartyEntities, parseContext))
                .withMessage(getExpectedCollectionTypeErrorMessage());
        }

        private String getExpectedCollectionTypeErrorMessage() {
            return String.format(
                SearchPartyValidator.COLLECTION_ERROR_MESSAGE, TEST_EXPRESSION_COLLECTION_FIELD_NAME_1);
        }

        private Exception prepareMockDotNotationValidatorToThrowForCollectionFieldName() {
            InvalidImportException exception = new InvalidImportException();
            doThrow(exception).when(dotNotationValidator).validateAndLoadFieldType(
                parseContext,
                SheetName.SEARCH_PARTY,
                ColumnName.SEARCH_PARTY_COLLECTION_FIELD_NAME,
                CASE_TYPE,
                TEST_EXPRESSION_BAD
            );

            return exception;
        }

        private Exception prepareMockDotNotationValidatorToThrow(ColumnName columnName) {
            InvalidImportException exception = new InvalidImportException();
            doThrow(exception).when(dotNotationValidator).validate(
                complexTypeUsedByCollection,
                SheetName.SEARCH_PARTY,
                columnName,
                TEST_EXPRESSION_BAD
            );

            return exception;
        }
    }

    private static SearchPartyEntity createBlankSearchPartyEntity() {

        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference(CASE_TYPE);

        SearchPartyEntity searchPartyEntity = new SearchPartyEntity();
        searchPartyEntity.setCaseType(caseTypeEntity);

        return searchPartyEntity;
    }

    private static SearchPartyEntity createPopulatedSearchPartyEntityWithoutCollectionField() {

        SearchPartyEntity searchPartyEntity = createBlankSearchPartyEntity();
        searchPartyEntity.setSearchPartyName(TEST_EXPRESSION_NAME_SINGLE_1);
        searchPartyEntity.setSearchPartyEmailAddress(TEST_EXPRESSION_EMAIL_1);
        searchPartyEntity.setSearchPartyAddressLine1(TEST_EXPRESSION_ADDRESS_1);
        searchPartyEntity.setSearchPartyPostCode(TEST_EXPRESSION_POSTCODE_1);
        searchPartyEntity.setSearchPartyDob(TEST_EXPRESSION_DOB_1);
        searchPartyEntity.setSearchPartyDod(TEST_EXPRESSION_DOD_1);
        searchPartyEntity.setSearchPartyCollectionFieldName(TEST_EXPRESSION_COLLECTION_FIELD_NAME_EMPTY);

        return searchPartyEntity;
    }

    private static SearchPartyEntity createPopulatedSearchPartyEntityWithCollectionField() {

        SearchPartyEntity searchPartyEntity = createBlankSearchPartyEntity();
        searchPartyEntity.setSearchPartyName(TEST_EXPRESSION_NAME_SINGLE_1);
        searchPartyEntity.setSearchPartyEmailAddress(TEST_EXPRESSION_EMAIL_1);
        searchPartyEntity.setSearchPartyAddressLine1(TEST_EXPRESSION_ADDRESS_1);
        searchPartyEntity.setSearchPartyPostCode(TEST_EXPRESSION_POSTCODE_1);
        searchPartyEntity.setSearchPartyDob(TEST_EXPRESSION_DOB_1);
        searchPartyEntity.setSearchPartyDod(TEST_EXPRESSION_DOD_1);
        searchPartyEntity.setSearchPartyCollectionFieldName(TEST_EXPRESSION_COLLECTION_FIELD_NAME_1);

        return searchPartyEntity;
    }

    private void verifyDotNotationValidatorCallMadeAtLeastOnce() {
        verify(dotNotationValidator, atLeastOnce()).validate(
            eq(parseContext),
            eq(SheetName.SEARCH_PARTY),
            any(ColumnName.class),
            eq(CASE_TYPE),
            anyString()
        );
    }

    private void verifyDotNotationValidatorWithComplexTypeCallMadeAtLeastOnce() {
        verify(dotNotationValidator, atLeastOnce()).validate(
            eq(complexTypeUsedByCollection),
            eq(SheetName.SEARCH_PARTY),
            any(ColumnName.class),
            anyString()
        );
    }

    private void verifyDotNotationValidatorCallMadeFor(ColumnName columnName, String expression) {
        verify(dotNotationValidator).validate(
            parseContext,
            SheetName.SEARCH_PARTY,
            columnName,
            CASE_TYPE,
            expression
        );
    }

    private void verifyDotNotationValidatorWithComplexTypeCallMadeFor(ColumnName columnName, String expression) {
        verify(dotNotationValidator).validate(
            complexTypeUsedByCollection,
            SheetName.SEARCH_PARTY,
            columnName,
            expression
        );
    }

    private void verifyDotNotationValidateCallMadeForCollectionFieldName(String expression) {
        verify(dotNotationValidator).validateAndLoadFieldType(
            parseContext,
            SheetName.SEARCH_PARTY,
            ColumnName.SEARCH_PARTY_COLLECTION_FIELD_NAME,
            CASE_TYPE,
            expression
        );
    }

    private void verifyDotNotationValidatorCallNeverMadeFor(ColumnName columnName) {
        verify(dotNotationValidator, never()).validate(
            eq(parseContext),
            eq(SheetName.SEARCH_PARTY),
            eq(columnName),
            eq(CASE_TYPE),
            anyString()
        );
        verify(dotNotationValidator, never()).validateAndLoadFieldType(
            eq(parseContext),
            eq(SheetName.SEARCH_PARTY),
            eq(columnName),
            eq(CASE_TYPE),
            anyString()
        );
        verify(dotNotationValidator, never()).validate(
            eq(complexTypeUsedByCollection),
            eq(SheetName.SEARCH_PARTY),
            eq(columnName),
            anyString()
        );
    }

    private FieldTypeEntity buildBaseCollectionType() {

        FieldTypeEntity baseCollectionType = new FieldTypeEntity();
        baseCollectionType.setReference(BASE_COLLECTION);

        return baseCollectionType;
    }

    private FieldTypeEntity buildBaseComplexType() {

        FieldTypeEntity baseComplexType = new FieldTypeEntity();
        baseComplexType.setReference(BASE_COMPLEX);

        return baseComplexType;
    }

    private FieldTypeEntity buildBaseTextType() {

        FieldTypeEntity baseTextType = new FieldTypeEntity();
        baseTextType.setReference(BASE_TEXT);

        return baseTextType;
    }

    private void setUpCollectionFieldNameFieldType() {

        // complex type for collection to use
        complexTypeUsedByCollection = new FieldTypeEntity();
        complexTypeUsedByCollection.setBaseFieldType(buildBaseComplexType());

        // add at least on complex field to complex type
        ComplexFieldEntity complexField = new ComplexFieldEntity();
        complexField.setReference("Text");
        complexField.setFieldType(buildBaseTextType());
        complexTypeUsedByCollection.getComplexFields().add(complexField);

        collectionFieldNameFieldType = new FieldTypeEntity();
        collectionFieldNameFieldType.setCollectionFieldType(complexTypeUsedByCollection);
        collectionFieldNameFieldType.setBaseFieldType(buildBaseCollectionType());
    }

    private void stubValidateWithoutCollection() {
        doNothing().when(dotNotationValidator).validate(
            eq(parseContext),
            eq(SheetName.SEARCH_PARTY),
            any(ColumnName.class),
            eq(CASE_TYPE),
            anyString()
        );
    }

    private void stubValidateWithCollection() {
        lenient().doReturn(collectionFieldNameFieldType).when(dotNotationValidator).validateAndLoadFieldType(
            eq(parseContext),
            eq(SheetName.SEARCH_PARTY),
            any(ColumnName.class),
            eq(CASE_TYPE),
            anyString()
        );
        lenient().doNothing().when(dotNotationValidator).validate(
            eq(complexTypeUsedByCollection),
            eq(SheetName.SEARCH_PARTY),
            any(ColumnName.class),
            anyString()
        );
    }
}
