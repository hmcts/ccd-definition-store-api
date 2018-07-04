package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;


import org.junit.Assert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class CaseFieldEntityMetadataFieldValidatorImplTest {

    private final CaseFieldEntityMetadataFieldValidatorImpl validator = new CaseFieldEntityMetadataFieldValidatorImpl();

    @Test
    @DisplayName("Should return validation result with exception when non-metadata case field id is enclosed in " +
        "square brackets")
    void shouldReturnValidationResultWithError_whenNonMetadataCaseFieldIdContainsSquareBrackets() {
        CaseFieldEntity caseField = new CaseFieldEntity();
        caseField.setReference("[FIELD]");
        CaseFieldEntityValidationContext context = mock(CaseFieldEntityValidationContext.class);
        doReturn("case ref").when(context).getCaseReference();

        ValidationResult result = validator.validate(caseField, context);

        assertThat(result.getValidationErrors(), hasSize(1));
        Assert.assertThat(result.getValidationErrors().get(0),
                          instanceOf(CaseFieldEntityInvalidMetadataFieldValidationError.class));
        Assert.assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
            "Invalid metadata field '[FIELD]' declaration for case type 'case ref'"));
    }

    @Test
    @DisplayName("Should return validation result with no errors when metadata case field has readonly display context")
    void shouldReturnValidationResultWithNoError_whenMetadataCaseFieldIdIsNotEnclosedInBrackets() {
        CaseFieldEntity caseField = new CaseFieldEntity();
        caseField.setReference("FIELD");
        CaseFieldEntityValidationContext context = mock(CaseFieldEntityValidationContext.class);

        ValidationResult result = validator.validate(caseField, context);

        assertThat(result.getValidationErrors(), hasSize(0));
    }
}
