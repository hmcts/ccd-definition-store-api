package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.datetime.InvalidDateTimeFormatException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.*;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.DisplayContextParameterValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.DisplayContextParameterValidatorFactory;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class ComplexFieldEntityDisplayContextParameterTest {

    private ComplexFieldEntityDisplayContextParameterValidatorImpl validator;

    @Mock
    private DisplayContextParameterValidatorFactory displayContextParameterValidatorFactory;

    @Mock
    private DisplayContextParameterValidator displayContextParameterValidator;

    @Mock
    private ComplexFieldValidator.ValidationContext caseFieldComplexFieldEntityValidator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new ComplexFieldEntityDisplayContextParameterValidatorImpl(displayContextParameterValidatorFactory);
        when(displayContextParameterValidatorFactory.getValidator(Mockito.any())).thenReturn(displayContextParameterValidator);
    }

    @Test
    void shouldValidateEntityWithNoDisplayContextParameter() {
        ComplexFieldEntity entity = complexFieldEntity("CASE_FIELD", fieldTypeEntity(FieldTypeUtils.BASE_TEXT));
        final ValidationResult result = validator.validate(entity, caseFieldComplexFieldEntityValidator);

        assertAll(
            () -> assertThat(result.isValid(), is(true))
        );
    }

    @Test
    void shouldValidateEntityWithDateTimeEntryDisplayContextParameter() {
        ComplexFieldEntity entity = complexFieldEntity("CASE_FIELD", fieldTypeEntity(FieldTypeUtils.BASE_DATE_TIME));
        entity.setDisplayContextParameter("#DATETIMEENTRY(hhmmss)");

        final ValidationResult result = validator.validate(entity, caseFieldComplexFieldEntityValidator);

        assertAll(
            () -> assertThat(result.isValid(), is(true))
        );
    }

    @Test
    void shouldValidateEntityWithDateTimeDisplayDisplayContextParameter() {
        ComplexFieldEntity entity = complexFieldEntity("CASE_FIELD", fieldTypeEntity(FieldTypeUtils.BASE_DATE_TIME));

        entity.setDisplayContextParameter("#DATETIMEDISPLAY(hhmmss)");

        final ValidationResult result = validator.validate(entity, caseFieldComplexFieldEntityValidator);

        assertAll(
            () -> assertThat(result.isValid(), is(true))
        );
    }

    @Test
    void shouldValidateEntityWithDisplayContextParameterForDateFieldType() {
        ComplexFieldEntity entity = complexFieldEntity("CASE_FIELD", fieldTypeEntity(FieldTypeUtils.BASE_DATE));
        entity.setDisplayContextParameter("#DATETIMEDISPLAY(hhmmss)");

        final ValidationResult result = validator.validate(entity, caseFieldComplexFieldEntityValidator);

        assertAll(
            () -> assertThat(result.isValid(), is(true))
        );
    }

    @Test
    void shouldValidateEntityWithDistinctSupportedDisplayContextParameterTypes() {
        ComplexFieldEntity entity = complexFieldEntity("CASE_FIELD", fieldTypeEntity(FieldTypeUtils.BASE_DATE));
        entity.setDisplayContextParameter("#DATETIMEDISPLAY(hhmmss),#DATETIMEENTRY(yyyy)");

        final ValidationResult result = validator.validate(entity, caseFieldComplexFieldEntityValidator);

        assertAll(
            () -> assertThat(result.isValid(), is(true))
        );
    }

    @Test
    void shouldFailValidationForDuplicateSupportedDisplayContextParameterTypes() {
        ComplexFieldEntity entity = complexFieldEntity("CASE_FIELD", fieldTypeEntity(FieldTypeUtils.BASE_DATE_TIME));

        entity.setDisplayContextParameter("#DATETIMEENTRY(yyyy),#DATETIMEENTRY(MM)");

        final ValidationResult result = validator.validate(entity, caseFieldComplexFieldEntityValidator);

        assertAll(
            () -> assertThat(result.isValid(), is(false)),
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                is("Display context parameter '#DATETIMEENTRY(yyyy),#DATETIMEENTRY(MM)'"
                   + " has been incorrectly configured or is invalid for field 'CASE_FIELD' on tab 'ComplexTypes'"))
        );
    }

    @Test
    void shouldFailValidationForInvalidDateTimeFormat() throws Exception {
        ComplexFieldEntity entity = complexFieldEntity("CASE_FIELD", fieldTypeEntity(FieldTypeUtils.BASE_DATE_TIME));

        entity.setDisplayContextParameter("#DATETIMEENTRY(0123456789)");
        doThrow(InvalidDateTimeFormatException.class).when(displayContextParameterValidator).validate(Mockito.any(), Mockito.any());

        final ValidationResult result = validator.validate(entity, caseFieldComplexFieldEntityValidator);

        assertAll(
            () -> assertThat(result.isValid(), is(false)),
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                is("Display context parameter '#DATETIMEENTRY(0123456789)' has been incorrectly configured or is invalid for field 'CASE_FIELD' on tab 'ComplexTypes'"))
        );
    }

    @Test
    void shouldFailValidationForInvalidDisplayContextParameterType() {
        ComplexFieldEntity entity = complexFieldEntity("CASE_FIELD", fieldTypeEntity(FieldTypeUtils.BASE_DATE_TIME));
        entity.setDisplayContextParameter("#INVALIDPARAMETER(hhmmss)");

        final ValidationResult result = validator.validate(entity, caseFieldComplexFieldEntityValidator);

        assertAll(
            () -> assertThat(result.isValid(), is(false)),
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                is("Display context parameter '#INVALIDPARAMETER(hhmmss)' has been incorrectly configured or is invalid for field 'CASE_FIELD' on tab 'ComplexTypes'"))
        );
    }

    @Test
    void shouldFailValidationForNotAllowedDisplayContextParameterType() {
        ComplexFieldEntity entity = complexFieldEntity("CASE_FIELD", fieldTypeEntity(FieldTypeUtils.BASE_DATE_TIME));
        entity.setDisplayContextParameter("#TABLE(FieldId)");

        final ValidationResult result = validator.validate(entity, caseFieldComplexFieldEntityValidator);

        assertAll(
            () -> assertThat(result.isValid(), is(false)),
            () -> assertThat(result.getValidationErrors().size(), is(2)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                is("Unsupported display context parameter type '#TABLE(FieldId)' for field 'CASE_FIELD' on tab 'ComplexTypes'"))
        );
    }

    @Test
    void shouldFailValidationForNotAllowedFieldType() {
        ComplexFieldEntity entity = complexFieldEntity("CASE_FIELD", fieldTypeEntity(FieldTypeUtils.BASE_TEXT));
        entity.setDisplayContextParameter("#DATETIMEDISPLAY(hhmmss)");

        final ValidationResult result = validator.validate(entity, caseFieldComplexFieldEntityValidator);

        assertAll(
            () -> assertThat(result.isValid(), is(false)),
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                is("Display context parameter '#DATETIMEDISPLAY(hhmmss)' is unsupported for field type 'Text' of field 'CASE_FIELD' on tab 'ComplexTypes'"))
        );
    }

    private static FieldTypeEntity fieldTypeEntity(String reference) {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(reference);
        return fieldTypeEntity;
    }

    private static ComplexFieldEntity complexFieldEntity(String reference, FieldTypeEntity fieldType) {
        ComplexFieldEntity complexFieldEntity = new ComplexFieldEntity();
        complexFieldEntity.setReference(reference);
        complexFieldEntity.setFieldType(fieldType);
        return complexFieldEntity;
    }
}
