package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;

import static uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityReferenceValidator.CASE_TYPE_ERROR_MESSAGE;
import static uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityReferenceValidator.NULL_REFERENCE;

@DisplayName("CaseType Entity Reference Validator Tests")
public class CaseTypeEntityReferenceValidatorTest {

    private CaseTypeEntity caseType;
    private final CaseTypeEntityReferenceValidator classUnderTest = new CaseTypeEntityReferenceValidator();

    @BeforeEach
    public void setUp() {
        caseType = new CaseTypeEntity();
    }

    @DisplayName("Should return empty validation result in case of no validation failures")
    @Test
    public void shouldReturnEmptyValidationResultWhenNoErrors() {
        assertAll(
            () -> assertValidReference("FT_MasterCaseType"),
            () -> assertValidReference("BEFTA_CASETYPE_1_1"),
            () -> assertValidReference("AllDataTypes2"),
            () -> assertValidReference("Asylum-XUI"),
            () -> assertValidReference("BUND_ASYNC_-1026083131"),
            () -> assertValidReference("EmpTrib_MVP_1.0_Glas")
        );

        caseType.setReference("Valid-CaseType1_with_underscore");

        final ValidationResult result = classUnderTest.validate(caseType);

        assertAll(
            () -> assertEquals(0, result.getValidationErrors().size()),
            () -> assertTrue(result.isValid())
        );
    }

    @DisplayName("Should return Validation Error when reference is invalid")
    @Test
    public void shouldReturnValidationResultWithValidationErrorWhenReferenceIsInvalid() {
        assertAll(
            () -> assertInvalidReference("With a space"),
            () -> assertInvalidReference("---"),
            () -> assertInvalidReference("_"),
            () -> assertInvalidReference(" "),
            () -> assertInvalidReference("*"),
            () -> assertInvalidReference("-_-")
        );
    }

    @DisplayName("Should return Validation Error when reference is null")
    @Test
    public void shouldReturnValidationResultWithValidationErrorWhenReferenceIsNull() {
        caseType.setReference(null);

        final ValidationResult result = classUnderTest.validate(caseType);

        assertAll(
            () -> assertEquals(1, result.getValidationErrors().size()),
            () -> assertFalse(result.isValid()),
            () -> assertEquals(result.getValidationErrors().get(0).getDefaultMessage(), NULL_REFERENCE)
        );
    }

    private void assertValidReference(String caseTypeReference) {
        caseType.setReference(caseTypeReference);

        final ValidationResult result = classUnderTest.validate(caseType);

        assertAll(
            () -> assertEquals(0, result.getValidationErrors().size(), caseTypeReference),
            () -> assertTrue(result.isValid())
        );
    }

    private void assertInvalidReference(String caseTypeReference) {
        caseType.setReference(caseTypeReference);

        final ValidationResult result = classUnderTest.validate(caseType);

        assertAll(
            () -> assertEquals(1, result.getValidationErrors().size(), caseTypeReference),
            () -> assertFalse(result.isValid()),
            () -> assertEquals(String.format(CASE_TYPE_ERROR_MESSAGE, caseTypeReference),
                               result.getValidationErrors().get(0).getDefaultMessage())
        );
    }
}


