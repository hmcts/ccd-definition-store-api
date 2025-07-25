package uk.gov.hmcts.ccd.definition.store.excel.validation;

import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchCriteriaEntity;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@DisplayName("SearchCriteriaValidator")
@ExtendWith(MockitoExtension.class)
class SearchCriteriaValidatorTest {

    private static final String CASE_TYPE = "TestCaseType";

    private static final String TEST_EXPRESSION_OTHER_CASE_REFERENCE_1 = "OtherCaseReference1";
    private static final String TEST_EXPRESSION_OTHER_CASE_REFERENCE_2 = "OtherCaseReference2";

    private static final String TEST_EXPRESSION_BAD = "BadTestExpression";

    @Mock
    private DotNotationValidator dotNotationValidator;

    @Mock
    private ParseContext parseContext;

    @InjectMocks
    private SearchCriteriaValidator searchCriteriaValidator;

    @DisplayName("should validate OtherCaseReference field")
    @Test
    void shouldValidateOtherCaseReferenceField() {

        // GIVEN
        SearchCriteriaEntity searchCriteriaEntity = createBlankSearchCriteriaEntity();
        searchCriteriaEntity.setOtherCaseReference(TEST_EXPRESSION_OTHER_CASE_REFERENCE_1);

        // WHEN
        searchCriteriaValidator.validate(List.of(searchCriteriaEntity), parseContext);

        // THEN
        verifyDotNotationValidatorCallMadeFor(ColumnName.OTHER_CASE_REFERENCE, TEST_EXPRESSION_OTHER_CASE_REFERENCE_1);
    }

    @DisplayName("should validate multiple SearchCriteriaEntity values")
    @Test
    void shouldValidateMultipleSearchCriteriaEntities() {

        // GIVEN
        SearchCriteriaEntity searchCriteriaEntity1 = createBlankSearchCriteriaEntity();
        searchCriteriaEntity1.setOtherCaseReference(TEST_EXPRESSION_OTHER_CASE_REFERENCE_1);

        SearchCriteriaEntity searchCriteriaEntity2 = createBlankSearchCriteriaEntity();
        searchCriteriaEntity2.setOtherCaseReference(TEST_EXPRESSION_OTHER_CASE_REFERENCE_2);

        // WHEN
        searchCriteriaValidator.validate(List.of(searchCriteriaEntity1, searchCriteriaEntity2), parseContext);

        // THEN
        verifyDotNotationValidatorCallMadeFor(ColumnName.OTHER_CASE_REFERENCE, TEST_EXPRESSION_OTHER_CASE_REFERENCE_1);
        verifyDotNotationValidatorCallMadeFor(ColumnName.OTHER_CASE_REFERENCE, TEST_EXPRESSION_OTHER_CASE_REFERENCE_2);
    }

    @DisplayName("should validate as ok even if OtherCaseReference is blank")
    @Test
    void shouldValidateAsOkEvenIfOtherCaseReferenceIsBlank() {

        // GIVEN
        SearchCriteriaEntity searchCriteriaEntity1 = createBlankSearchCriteriaEntity();
        searchCriteriaEntity1.setOtherCaseReference("");

        SearchCriteriaEntity searchCriteriaEntity2 = createBlankSearchCriteriaEntity();
        searchCriteriaEntity2.setOtherCaseReference(null);

        // WHEN
        searchCriteriaValidator.validate(List.of(searchCriteriaEntity1, searchCriteriaEntity2), parseContext);

        // THEN
        verifyDotNotationValidatorCallNeverMadeFor(ColumnName.OTHER_CASE_REFERENCE);
    }

    @DisplayName("throws exception if OtherCaseReference validation fails")
    @Test
    void throwsExceptionIfOtherCaseReferenceValidationFails() {

        // GIVEN
        SearchCriteriaEntity searchCriteriaEntity1 = createBlankSearchCriteriaEntity();
        searchCriteriaEntity1.setOtherCaseReference(TEST_EXPRESSION_BAD);
        Exception expectedException = prepareMockDotNotationValidatorToThrow(ColumnName.OTHER_CASE_REFERENCE);

        // WHEN & THEN
        Exception actualException = assertThrows(expectedException.getClass(), () ->
            searchCriteriaValidator.validate(List.of(searchCriteriaEntity1), parseContext)
        );

        assertEquals(expectedException, actualException);
    }

    private static SearchCriteriaEntity createBlankSearchCriteriaEntity() {

        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference(CASE_TYPE);

        SearchCriteriaEntity searchCriteriaEntity = new SearchCriteriaEntity();
        searchCriteriaEntity.setCaseType(caseTypeEntity);

        return searchCriteriaEntity;
    }

    @SuppressWarnings("SameParameterValue")
    private void verifyDotNotationValidatorCallMadeFor(ColumnName columnName, String expression) {
        verify(dotNotationValidator).validate(
            parseContext,
            SheetName.SEARCH_CRITERIA,
            columnName,
            CASE_TYPE,
            expression
        );
    }

    @SuppressWarnings("SameParameterValue")
    private void verifyDotNotationValidatorCallNeverMadeFor(ColumnName columnName) {
        verify(dotNotationValidator, never()).validate(
            eq(parseContext),
            eq(SheetName.SEARCH_CRITERIA),
            eq(columnName),
            eq(CASE_TYPE),
            anyString()
        );
    }

    @SuppressWarnings("SameParameterValue")
    private Exception prepareMockDotNotationValidatorToThrow(ColumnName columnName) {
        InvalidImportException exception = new InvalidImportException();
        doThrow(exception).when(dotNotationValidator).validate(
            parseContext,
            SheetName.SEARCH_CRITERIA,
            columnName,
            CASE_TYPE,
            TEST_EXPRESSION_BAD
        );

        return exception;
    }

}
