package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CaseFieldEntityMetadataFieldValidatorImplTest {

    private final CaseFieldEntityMetadataFieldValidatorImpl validator = new CaseFieldEntityMetadataFieldValidatorImpl();

    @Test
    @DisplayName(
        "Should return validation result with exception when non-metadata case field id is enclosed in square brackets")
    void shouldReturnValidationResultWithError_whenNonMetadataCaseFieldIdContainsSquareBrackets() {
        CaseFieldEntity caseField = new CaseFieldEntity();
        caseField.setReference("[FIELD]");
        CaseFieldEntityValidationContext context = mock(CaseFieldEntityValidationContext.class);
        when(context.getCaseReference()).thenReturn("case ref");

        ValidationResult result = validator.validate(caseField, context);

        assertThat(result.getValidationErrors(), hasSize(1));
        assertThat(result.getValidationErrors().get(0),
            instanceOf(CaseFieldEntityInvalidMetadataFieldValidationError.class));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
            is("Metadata field syntax cannot be used for a case field. Found '[FIELD]'"));
    }

    @Test
    @DisplayName(
        "Should return no exception when metadata field name is used as case field name without square brackets")
    void shouldReturnValidationResultWithNoError_whenMetadataCaseFieldIdIsNotEnclosedInBrackets() {
        CaseFieldEntity caseField = new CaseFieldEntity();
        caseField.setReference("STATE");
        CaseFieldEntityValidationContext context = mock(CaseFieldEntityValidationContext.class);
        when(context.getCaseReference()).thenReturn("case ref");

        ValidationResult result = validator.validate(caseField, context);

        assertThat(result.getValidationErrors(), hasSize(0));
    }
}
