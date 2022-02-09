package uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.DisplayContextParameterValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.DisplayContextParameterValidatorFactory;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class DisplayGroupArgumentDisplayContextParameterValidatorImplTest {

    private DisplayGroupArgumentDisplayContextParameterValidatorImpl validator;
    private static final String TEST_ARGUMENT = "#ARGUMENT(TestArgument)";

    @Mock
    private DisplayContextParameterValidatorFactory displayContextParameterValidatorFactory;

    @Mock
    private DisplayContextParameterValidator displayContextParameterValidator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new DisplayGroupArgumentDisplayContextParameterValidatorImpl(
            displayContextParameterValidatorFactory);
        when(displayContextParameterValidatorFactory.getValidator(Mockito.any()))
            .thenReturn(displayContextParameterValidator);
    }

    @Test
    void shouldValidateEntityWithNoDisplayContextParameter() {
        DisplayGroupCaseFieldEntity entity = new DisplayGroupCaseFieldEntity();
        entity.setCaseField(caseFieldEntity());

        final ValidationResult result = validator.validate(entity);

        assertTrue(result.isValid());
    }

    @Test
    void shouldValidateEntityWithArgumentDisplayContextParameter() {
        DisplayGroupCaseFieldEntity entity = new DisplayGroupCaseFieldEntity();
        entity.setDisplayContextParameter(TEST_ARGUMENT);
        entity.setCaseField(caseFieldEntity());

        final ValidationResult result = validator.validate(entity);

        assertTrue(result.isValid());
    }

    @Test
    void shouldReturnSheetName() {
        DisplayGroupCaseFieldEntity entity = new DisplayGroupCaseFieldEntity();
        entity.setCaseField(caseFieldEntity());

        assertEquals("CaseTypeTab", validator.getSheetName(entity));
    }

    @Test
    void shouldReturnCaseReferenceWhenPresent() {
        DisplayGroupCaseFieldEntity entity = new DisplayGroupCaseFieldEntity();
        entity.setCaseField(caseFieldEntity());

        assertEquals("CASE_FIELD", validator.getCaseFieldReference(entity));
    }

    @Test
    void shouldReturnEmptyCaseReferenceWhenNotSet() {
        DisplayGroupCaseFieldEntity entity = new DisplayGroupCaseFieldEntity();
        assertTrue(validator.getCaseFieldReference(entity).isEmpty());
    }

    @Test
    void shouldReturnFieldType() {
        DisplayGroupCaseFieldEntity entity = new DisplayGroupCaseFieldEntity();
        entity.setCaseField(caseFieldEntity());

        assertEquals(FieldTypeUtils.BASE_TEXT, validator.getFieldTypeEntity(entity).getReference());
    }

    @Test
    void shouldReturnDisplayContextParameter() {
        DisplayGroupCaseFieldEntity entity = new DisplayGroupCaseFieldEntity();
        entity.setDisplayContextParameter(TEST_ARGUMENT);
        entity.setCaseField(caseFieldEntity());

        assertEquals(TEST_ARGUMENT, validator.getDisplayContextParameter(entity));
    }

    @Test
    void shouldValidateEntityWithDateTimeDisplayDisplayContextParameter() {
        DisplayGroupCaseFieldEntity entity = new DisplayGroupCaseFieldEntity();
        entity.setDisplayContextParameter("#DATETIMEDISPLAY(hhmmss)");
        entity.setCaseField(caseFieldEntity());

        final ValidationResult result = validator.validate(entity);

        assertTrue(result.isValid());
    }

    private static CaseFieldEntity caseFieldEntity() {
        return caseFieldEntity(fieldTypeEntity(FieldTypeUtils.BASE_TEXT));
    }

    private static CaseFieldEntity caseFieldEntity(FieldTypeEntity fieldTypeEntity) {
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setReference("CASE_FIELD");
        caseFieldEntity.setFieldType(fieldTypeEntity);
        return caseFieldEntity;
    }

    private static FieldTypeEntity fieldTypeEntity(String fieldType) {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(fieldType);
        return fieldTypeEntity;
    }
}

