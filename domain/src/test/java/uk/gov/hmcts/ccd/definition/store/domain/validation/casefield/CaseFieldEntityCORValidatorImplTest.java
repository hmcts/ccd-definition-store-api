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

class CaseFieldEntityCORValidatorImplTest {

    private final CaseFieldEntityCORValidatorImpl validator = new CaseFieldEntityCORValidatorImpl();


    @Test
    @DisplayName(
        "Should return validation result with exception when Change of Request field is defined twice in one case type")
    void shouldReturnValidationResultWithError_whenCORDefinedTwiceInCaseType() {

        FieldTypeEntity fieldType = new FieldTypeEntity();
        fieldType.setReference("ChangeOrganisationRequest");
        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference("case type");

        CaseFieldEntity caseField1 = new CaseFieldEntity();
        caseField1.setFieldType(fieldType);
        caseField1.setCaseType(caseType);

        CaseFieldEntity caseField2 = new CaseFieldEntity();
        caseField2.setFieldType(fieldType);
        caseField2.setCaseType(caseType);

        CaseFieldEntityValidationContext context = mock(CaseFieldEntityValidationContext.class);
        when(context.getCaseReference()).thenReturn("case ref");

        validator.validate(caseField1, context);
        ValidationResult result = validator.validate(caseField2, context);

        assertThat(result.getValidationErrors(), hasSize(1));
        assertThat(result.getValidationErrors().get(0),
            instanceOf(CaseFieldEntityCORValidationError.class));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
            is("Change Organisation Request is defined more than once for case type 'case type'"));
    }

    @Test
    @DisplayName(
        "Should return no exception when Change Organisation Request is defined once in a case type")
    void shouldReturnValidationResultWithNoError_whenCORDefinedOnceInCaseType() {

        FieldTypeEntity fieldType = new FieldTypeEntity();
        fieldType.setReference("ChangeOrganisationRequest");
        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference("case type");

        CaseFieldEntity caseField = new CaseFieldEntity();
        caseField.setFieldType(fieldType);
        caseField.setCaseType(caseType);

        CaseFieldEntityValidationContext context = mock(CaseFieldEntityValidationContext.class);
        when(context.getCaseReference()).thenReturn("case ref");

        ValidationResult result = validator.validate(caseField, context);

        assertThat(result.getValidationErrors(), hasSize(0));
    }

    @Test
    @DisplayName(
        "Should return no exception when Change Organisation Request is defined once in multiple case types")
    void shouldReturnValidationResultWithNoError_whenCORDefinedOnceInMultipleCaseType() {

        FieldTypeEntity fieldType = new FieldTypeEntity();
        fieldType.setReference("ChangeOrganisationRequest");
        CaseTypeEntity caseType1 = new CaseTypeEntity();
        CaseTypeEntity caseType2 = new CaseTypeEntity();
        caseType1.setReference("case type 1");
        caseType2.setReference("case type 2");

        CaseFieldEntity caseField1 = new CaseFieldEntity();
        caseField1.setFieldType(fieldType);
        caseField1.setCaseType(caseType1);

        CaseFieldEntity caseField2 = new CaseFieldEntity();
        caseField2.setFieldType(fieldType);
        caseField2.setCaseType(caseType2);

        CaseFieldEntityValidationContext context = mock(CaseFieldEntityValidationContext.class);
        when(context.getCaseReference()).thenReturn("case ref");

        validator.validate(caseField1, context);
        ValidationResult result = validator.validate(caseField2, context);

        assertThat(result.getValidationErrors(), hasSize(0));


    }
}
