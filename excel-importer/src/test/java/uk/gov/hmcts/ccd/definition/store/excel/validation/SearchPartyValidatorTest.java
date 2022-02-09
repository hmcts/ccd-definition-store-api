package uk.gov.hmcts.ccd.definition.store.excel.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@DisplayName("SearchPartyValidator")
@ExtendWith(MockitoExtension.class)
class SearchPartyValidatorTest {
    private static final String NAME_FIELD_SEPARATOR = ",";

    private static final String CASE_TYPE = "TestCaseType";

    private static final String TEST_EXPRESSION_NAME_SINGLE_1 = "Name1";
    private static final String TEST_EXPRESSION_NAME_SINGLE_2 = "Name2";
    private static final String TEST_EXPRESSION_NAME_MULTIPLE =
        TEST_EXPRESSION_NAME_SINGLE_1 + NAME_FIELD_SEPARATOR + TEST_EXPRESSION_NAME_SINGLE_2;

    private static final String TEST_EXPRESSION_EMAIL_1 = "Email1";
    private static final String TEST_EXPRESSION_ADDRESS_1 = "Address1";
    private static final String TEST_EXPRESSION_POSTCODE_1 = "Postcode1";
    private static final String TEST_EXPRESSION_DOB_1 = "DOB1";
    private static final String TEST_EXPRESSION_DOD_1 = "DOD1";

    private static final String TEST_EXPRESSION_COLLECTION_FIELD_NAME_EMPTY = "";
    private static final String TEST_EXPRESSION_COLLECTION_FIELD_NAME_1 = "Name1";

    private static final String TEST_EXPRESSION_BAD = "BadTestExpression";

    @Mock
    private DotNotationValidator dotNotationValidator;

    @Mock
    private ParseContext parseContext;

    @InjectMocks
    private SearchPartyValidator searchPartyValidator;

    @BeforeEach
    void setUp() {
        // default stubbing must be explicitly set to prevent PotentialStubbingProblem when testing throws
        doNothing().when(dotNotationValidator).validate(
            eq(parseContext),
            eq(SheetName.SEARCH_PARTY),
            any(ColumnName.class),
            eq(CASE_TYPE),
            anyString()
        );
    }

    @DisplayName("should validate all fields without collection field")
    @Test
    void shouldValidateAllFieldsWithoutCollectionField() {

        // GIVEN
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

    @DisplayName("should validate all fields")
    @Test
    void shouldValidateAllFields() {

        // GIVEN
        FieldTypeEntity caseFieldType = createFieldTypeEntity();
        doReturn(caseFieldType).when(parseContext).getCaseFieldType(CASE_TYPE, TEST_EXPRESSION_COLLECTION_FIELD_NAME_1);

        SearchPartyEntity searchPartyEntity1 = createPopulatedSearchPartyEntityWithCollectionField();

        // WHEN
        searchPartyValidator.validate(List.of(searchPartyEntity1), parseContext);

        // THEN
        verifyDotNotationValidatorCallNeverMadeFor(ColumnName.SEARCH_PARTY_NAME);
        verifyDotNotationValidatorCallNeverMadeFor(ColumnName.SEARCH_PARTY_EMAIL_ADDRESS);
        verifyDotNotationValidatorCallNeverMadeFor(ColumnName.SEARCH_PARTY_ADDRESS_LINE_1);
        verifyDotNotationValidatorCallNeverMadeFor(ColumnName.SEARCH_PARTY_POST_CODE);
        verifyDotNotationValidatorCallNeverMadeFor(ColumnName.SEARCH_PARTY_DOB);
        verifyDotNotationValidatorCallNeverMadeFor(ColumnName.SEARCH_PARTY_DOD);

        verifyDotNotationValidatorCallMadeFor(ColumnName.SEARCH_PARTY_COLLECTION_FIELD_NAME,
                                              searchPartyEntity1.getSearchPartyCollectionFieldName());
    }

    @DisplayName("should raise exception when SearchPartyCollectionFieldName is not a collection or complex type")
    @ParameterizedTest
    @ValueSource(strings = {"NonCollection", "Collection"})
    void shouldValidateSearchPartyCollectionFieldNamePart1(final String fieldType) {
        // GIVEN
        FieldTypeEntity collectionCaseFieldType = new FieldTypeEntity();
        collectionCaseFieldType.setReference(fieldType);

        FieldTypeEntity caseFieldType = new FieldTypeEntity();
        caseFieldType.setBaseFieldType(collectionCaseFieldType);
        caseFieldType.setCollectionFieldType(collectionCaseFieldType);

        doReturn(caseFieldType).when(parseContext).getCaseFieldType(CASE_TYPE, TEST_EXPRESSION_COLLECTION_FIELD_NAME_1);

        SearchPartyEntity searchPartyEntity1 = createPopulatedSearchPartyEntityWithCollectionField();

        // WHEN/THEN
        assertThatExceptionOfType(InvalidImportException.class)
            .isThrownBy(() -> searchPartyValidator.validate(List.of(searchPartyEntity1), parseContext))
            .withMessage("SearchPartyTab Invalid value 'Name1' "
                + "is not a valid SearchPartyCollectionFieldName value as it does not"
                + " reference a collection of a complex type.");
    }

    @DisplayName("should validate multiple searchPartyEntity values")
    @Test
    void shouldValidateMultipleSearchPartyEntities() {

        // GIVEN
        SearchPartyEntity searchPartyEntity1 = createBlankSearchPartyEntity();
        searchPartyEntity1.setSearchPartyName(TEST_EXPRESSION_NAME_SINGLE_1);

        SearchPartyEntity searchPartyEntity2 = createBlankSearchPartyEntity();
        searchPartyEntity2.setSearchPartyName(TEST_EXPRESSION_NAME_SINGLE_2);

        // WHEN
        searchPartyValidator.validate(List.of(searchPartyEntity1, searchPartyEntity2), parseContext);

        // THEN
        verifyDotNotationValidatorCallMadeFor(ColumnName.SEARCH_PARTY_NAME, TEST_EXPRESSION_NAME_SINGLE_1);
        verifyDotNotationValidatorCallMadeFor(ColumnName.SEARCH_PARTY_NAME, TEST_EXPRESSION_NAME_SINGLE_2);
    }

    @DisplayName("should validate SearchPartyName when supplied as CSV")
    @Test
    void shouldValidateSearchPartyNameCsv() {

        // GIVEN
        SearchPartyEntity searchPartyEntity1 = createBlankSearchPartyEntity();
        searchPartyEntity1.setSearchPartyName(TEST_EXPRESSION_NAME_MULTIPLE);

        // WHEN
        searchPartyValidator.validate(List.of(searchPartyEntity1), parseContext);

        // THEN
        verifyDotNotationValidatorCallMadeFor(ColumnName.SEARCH_PARTY_NAME, TEST_EXPRESSION_NAME_SINGLE_1);
        verifyDotNotationValidatorCallMadeFor(ColumnName.SEARCH_PARTY_NAME, TEST_EXPRESSION_NAME_SINGLE_2);
    }

    @DisplayName("should validate as ok even if SearchPartyName is blank")
    @Test
    void shouldValidateAsOkEvenIfSearchPartyNameIsBlank() {

        // GIVEN
        SearchPartyEntity searchPartyEntity1 = createPopulatedSearchPartyEntityWithoutCollectionField();
        searchPartyEntity1.setSearchPartyName("");

        SearchPartyEntity searchPartyEntity2 = createPopulatedSearchPartyEntityWithoutCollectionField();
        searchPartyEntity2.setSearchPartyName(null);

        // WHEN
        searchPartyValidator.validate(List.of(searchPartyEntity1, searchPartyEntity2), parseContext);

        // THEN
        verifyDotNotationValidatorCallMadeAtLeastOnce(); // verify at least one validation ..
        verifyDotNotationValidatorCallNeverMadeFor(ColumnName.SEARCH_PARTY_NAME); //.. but none for blank field
    }

    @DisplayName("should validate as ok even if SearchPartyEmailAddress is blank")
    @Test
    void shouldValidateAsOkEvenIfSearchPartyEmailAddressIsBlank() {

        // GIVEN
        SearchPartyEntity searchPartyEntity1 = createPopulatedSearchPartyEntityWithoutCollectionField();
        searchPartyEntity1.setSearchPartyEmailAddress("");

        SearchPartyEntity searchPartyEntity2 = createPopulatedSearchPartyEntityWithoutCollectionField();
        searchPartyEntity2.setSearchPartyEmailAddress(null);

        // WHEN
        searchPartyValidator.validate(List.of(searchPartyEntity1, searchPartyEntity2), parseContext);

        // THEN
        verifyDotNotationValidatorCallMadeAtLeastOnce(); // verify at least one validation ..
        verifyDotNotationValidatorCallNeverMadeFor(ColumnName.SEARCH_PARTY_EMAIL_ADDRESS); //..but none for blank field
    }

    @DisplayName("should validate as ok even if SearchPartyAddressLine1 is blank")
    @Test
    void shouldValidateAsOkEvenIfSearchPartyAddressLine1IsBlank() {

        // GIVEN
        SearchPartyEntity searchPartyEntity1 = createPopulatedSearchPartyEntityWithoutCollectionField();
        searchPartyEntity1.setSearchPartyAddressLine1("");

        SearchPartyEntity searchPartyEntity2 = createPopulatedSearchPartyEntityWithoutCollectionField();
        searchPartyEntity2.setSearchPartyAddressLine1(null);

        // WHEN
        searchPartyValidator.validate(List.of(searchPartyEntity1, searchPartyEntity2), parseContext);

        // THEN
        verifyDotNotationValidatorCallMadeAtLeastOnce(); // verify at least one validation ..
        verifyDotNotationValidatorCallNeverMadeFor(ColumnName.SEARCH_PARTY_ADDRESS_LINE_1); //..but none for blank field
    }

    @DisplayName("should validate as ok even if SearchPartyPostCode is blank")
    @Test
    void shouldValidateAsOkEvenIfSearchPartyPostCodeIsBlank() {

        // GIVEN
        SearchPartyEntity searchPartyEntity1 = createPopulatedSearchPartyEntityWithoutCollectionField();
        searchPartyEntity1.setSearchPartyPostCode("");

        SearchPartyEntity searchPartyEntity2 = createPopulatedSearchPartyEntityWithoutCollectionField();
        searchPartyEntity2.setSearchPartyPostCode(null);

        // WHEN
        searchPartyValidator.validate(List.of(searchPartyEntity1, searchPartyEntity2), parseContext);

        // THEN
        verifyDotNotationValidatorCallMadeAtLeastOnce(); // verify at least one validation ..
        verifyDotNotationValidatorCallNeverMadeFor(ColumnName.SEARCH_PARTY_POST_CODE); //..but none for blank field
    }

    @DisplayName("should validate as ok even if SearchPartyDob is blank")
    @Test
    void shouldValidateAsOkEvenIfSearchPartyDobIsBlank() {

        // GIVEN
        SearchPartyEntity searchPartyEntity1 = createPopulatedSearchPartyEntityWithoutCollectionField();
        searchPartyEntity1.setSearchPartyDob("");

        SearchPartyEntity searchPartyEntity2 = createPopulatedSearchPartyEntityWithoutCollectionField();
        searchPartyEntity2.setSearchPartyDob(null);

        // WHEN
        searchPartyValidator.validate(List.of(searchPartyEntity1, searchPartyEntity2), parseContext);

        // THEN
        verifyDotNotationValidatorCallMadeAtLeastOnce(); // verify at least one validation ..
        verifyDotNotationValidatorCallNeverMadeFor(ColumnName.SEARCH_PARTY_DOB); //..but none for blank field
    }

    @DisplayName("should validate as ok even if SearchPartyDod is blank")
    @Test
    void shouldValidateAsOkEvenIfSearchPartyDodIsBlank() {

        // GIVEN
        SearchPartyEntity searchPartyEntity1 = createPopulatedSearchPartyEntityWithoutCollectionField();
        searchPartyEntity1.setSearchPartyDod("");

        SearchPartyEntity searchPartyEntity2 = createPopulatedSearchPartyEntityWithoutCollectionField();
        searchPartyEntity2.setSearchPartyDod(null);

        // WHEN
        searchPartyValidator.validate(List.of(searchPartyEntity1, searchPartyEntity2), parseContext);

        // THEN
        verifyDotNotationValidatorCallMadeAtLeastOnce(); // verify at least one validation ..
        verifyDotNotationValidatorCallNeverMadeFor(ColumnName.SEARCH_PARTY_DOD); //..but none for blank field
    }

    @DisplayName("throws exception if SearchPartyName validation fails")
    @Test
    void throwsExceptionIfSearchPartyNameValidationFails() {

        // GIVEN
        SearchPartyEntity searchPartyEntity1 = createPopulatedSearchPartyEntityWithoutCollectionField();
        searchPartyEntity1.setSearchPartyName(
            // NB: 1 good expression and 1 bad expression
            TEST_EXPRESSION_NAME_SINGLE_1 + NAME_FIELD_SEPARATOR + TEST_EXPRESSION_BAD
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
        Exception expectedException = prepareMockDotNotationValidatorToThrow(ColumnName.SEARCH_PARTY_ADDRESS_LINE_1);

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

    private static FieldTypeEntity createFieldTypeEntity() {
        FieldTypeEntity collectionCaseFieldType = new FieldTypeEntity();
        collectionCaseFieldType.setReference("Collection");
        collectionCaseFieldType.addComplexFields(List.of(new ComplexFieldEntity()));

        FieldTypeEntity caseFieldType = new FieldTypeEntity();
        caseFieldType.setBaseFieldType(collectionCaseFieldType);
        caseFieldType.setCollectionFieldType(collectionCaseFieldType);

        return caseFieldType;
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

    private void verifyDotNotationValidatorCallMadeFor(ColumnName columnName, String expression) {
        verify(dotNotationValidator).validate(
            parseContext,
            SheetName.SEARCH_PARTY,
            columnName,
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
