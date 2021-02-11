package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CaseFieldEntityCORValidatorImplTest {

    private final CaseFieldEntityCORValidatorImpl validator = new CaseFieldEntityCORValidatorImpl();


    @Test
    @DisplayName(
        "Should return validation result with exception when Change of Request field is associated with an invalid ID")
    void shouldReturnValidationResultWithError_whenCORFieldDefinedWithInvalidID() {

        FieldTypeEntity fieldType = new FieldTypeEntity();
        fieldType.setReference("ChangeOrganisationRequest");
        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference("Any Case Type");
        CaseFieldEntity caseField = new CaseFieldEntity();
        caseField.setReference("random ID");
        caseField.setFieldType(fieldType);
        caseField.setCaseType(caseType);

        CaseFieldEntityValidationContext context = mock(CaseFieldEntityValidationContext.class);
        when(context.getCaseReference()).thenReturn("case ref");

        ValidationResult result = validator.validate(caseField, context);

        assertThat(result.getValidationErrors(), hasSize(1));
        assertThat(result.getValidationErrors().get(0),
            instanceOf(CaseFieldEntityCORValidationError.class));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
            is("The Change Organisation Request FieldType must be associated with an ID of "
                + "'ChangeOrganisationRequest' instead of 'random ID' and "
                + "may only be defined once in CaseType 'Any Case Type'"));
    }

    @Test
    @DisplayName(
        "Should return no exception when Change Organisation Request is defined with correct ID")
    void shouldReturnValidationResultWithNoError_whenCORDefinedOnceInCaseType() {

        FieldTypeEntity fieldType = new FieldTypeEntity();
        fieldType.setReference("ChangeOrganisationRequest");
        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference("CaseType");
        CaseFieldEntity caseField = new CaseFieldEntity();
        caseField.setReference("ChangeOrganisationRequest");
        caseField.setFieldType(fieldType);
        caseField.setCaseType(caseType);


        CaseFieldEntityValidationContext context = mock(CaseFieldEntityValidationContext.class);
        when(context.getCaseReference()).thenReturn("case ref");

        ValidationResult result = validator.validate(caseField, context);

        assertThat(result.getValidationErrors(), hasSize(0));
    }

    @Test
    @DisplayName(
        "Should return no exception when ChangeOrganisationRequest ID has a Non-COR FieldType")
    void shouldReturnValidationResultWithNoError_whenNonCORDefined() {

        FieldTypeEntity fieldType = new FieldTypeEntity();
        fieldType.setReference("Address1");
        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference("CaseType");
        CaseFieldEntity caseField = new CaseFieldEntity();
        caseField.setReference("ChangeOrganisationRequest");
        caseField.setFieldType(fieldType);
        caseField.setCaseType(caseType);

        CaseFieldEntityValidationContext context = mock(CaseFieldEntityValidationContext.class);
        when(context.getCaseReference()).thenReturn("case ref");

        ValidationResult result = validator.validate(caseField, context);

        assertThat(result.getValidationErrors(), hasSize(0));
    }
}
