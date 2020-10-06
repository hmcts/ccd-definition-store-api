package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.datetime.InvalidDateTimeFormatException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.DisplayContextParameterValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.DisplayContextParameterValidatorFactory;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class EventCaseFieldDateTimeDisplayContextParameterValidatorImplTest {

    private EventCaseFieldEntityValidator validator;

    @Mock
    private DisplayContextParameterValidatorFactory displayContextParameterValidatorFactory;

    @Mock
    private DisplayContextParameterValidator displayContextParameterValidator;

    @Mock
    private EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new EventCaseFieldDateTimeDisplayContextParameterValidatorImpl(
            displayContextParameterValidatorFactory);
        when(displayContextParameterValidatorFactory.getValidator(Mockito.any()))
            .thenReturn(displayContextParameterValidator);
    }

    @Test
    void shouldValidateEntityWithNoDisplayContextParameter() {
        EventCaseFieldEntity entity = new EventCaseFieldEntity();
        entity.setCaseField(caseFieldEntity());

        final ValidationResult result = validator.validate(entity, eventCaseFieldEntityValidationContext);

        assertAll(
            () -> assertThat(result.isValid(), is(true))
        );
    }

    @Test
    void shouldValidateEntityWithTableDisplayContextParameter() {
        EventCaseFieldEntity entity = new EventCaseFieldEntity();
        entity.setDisplayContextParameter("#TABLE(FieldId)");
        entity.setCaseField(caseFieldEntity());

        final ValidationResult result = validator.validate(entity, eventCaseFieldEntityValidationContext);

        assertAll(
            () -> assertThat(result.isValid(), is(true))
        );
    }

    @Test
    void shouldValidateEntityWithDateTimeDisplayDisplayContextParameter() {
        EventCaseFieldEntity entity = new EventCaseFieldEntity();
        entity.setDisplayContextParameter("#DATETIMEDISPLAY(hhmmss)");
        entity.setCaseField(caseFieldEntity());
        entity.setDisplayContext(DisplayContext.READONLY);

        final ValidationResult result = validator.validate(entity, eventCaseFieldEntityValidationContext);

        assertAll(
            () -> assertThat(result.isValid(), is(true))
        );
    }

    @Test
    void shouldValidateEntityWithDisplayContextParameterForDateFieldType() {
        EventCaseFieldEntity entity = new EventCaseFieldEntity();
        entity.setDisplayContextParameter("#DATETIMEDISPLAY(hhmmss)");
        entity.setCaseField(caseFieldEntity(fieldTypeEntity(FieldTypeUtils.BASE_DATE)));
        entity.setDisplayContext(DisplayContext.READONLY);

        final ValidationResult result = validator.validate(entity, eventCaseFieldEntityValidationContext);

        assertAll(
            () -> assertThat(result.isValid(), is(true))
        );
    }

    @Test
    void shouldValidateReadonlyFieldWithDateTimeDisplay() {
        EventCaseFieldEntity entity = new EventCaseFieldEntity();
        entity.setDisplayContextParameter("#DATETIMEDISPLAY(HHmmss)");
        entity.setDisplayContext(DisplayContext.READONLY);
        entity.setCaseField(caseFieldEntity());

        final ValidationResult result = validator.validate(entity, eventCaseFieldEntityValidationContext);

        assertAll(
            () -> assertThat(result.isValid(), is(true))
        );
    }

    @Test
    void shouldFailValidationForReadonlyFieldWithDateTimeEntry() {
        EventCaseFieldEntity entity = new EventCaseFieldEntity();
        entity.setDisplayContextParameter("#DATETIMEENTRY(HHmmss)");
        entity.setDisplayContext(DisplayContext.READONLY);
        entity.setCaseField(caseFieldEntity());

        final ValidationResult result = validator.validate(entity, eventCaseFieldEntityValidationContext);

        assertAll(
            () -> assertThat(result.isValid(), is(false)),
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                is("Unsupported display context parameter type '#DATETIMEENTRY(HHmmss)' for field 'CASE_FIELD' "
                    + "on tab 'CaseEventToFields'"))
        );
    }

    @Test
    void shouldFailValidationForInvalidDateTimeFormat() throws Exception {
        EventCaseFieldEntity entity = new EventCaseFieldEntity();
        entity.setDisplayContextParameter("#DATETIMEENTRY(0123456789)");
        entity.setCaseField(caseFieldEntity());
        entity.setDisplayContext(DisplayContext.MANDATORY);
        doThrow(InvalidDateTimeFormatException.class)
            .when(displayContextParameterValidator).validate(Mockito.any(), Mockito.any());

        final ValidationResult result = validator.validate(entity, eventCaseFieldEntityValidationContext);

        assertAll(
            () -> assertThat(result.isValid(), is(false)),
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                is("Display context parameter '#DATETIMEENTRY(0123456789)' has been incorrectly "
                    + "configured or is invalid for field 'CASE_FIELD' on tab 'CaseEventToFields'"))
        );
    }

    @Test
    void shouldSkipValidationForInvalidDisplayContextParameterType() {
        EventCaseFieldEntity entity = new EventCaseFieldEntity();
        entity.setDisplayContextParameter("#INVALIDPARAMETER(HHmmss)");
        entity.setCaseField(caseFieldEntity());

        final ValidationResult result = validator.validate(entity, eventCaseFieldEntityValidationContext);

        assertAll(
            () -> assertThat(result.isValid(), is(true))
        );
    }

    @Test
    void shouldFailValidationForNotAllowedFieldType() {
        EventCaseFieldEntity entity = new EventCaseFieldEntity();
        entity.setDisplayContextParameter("#DATETIMEDISPLAY(HHmmss)");
        entity.setCaseField(caseFieldEntity(fieldTypeEntity(FieldTypeUtils.BASE_TEXT)));

        final ValidationResult result = validator.validate(entity, eventCaseFieldEntityValidationContext);

        assertAll(
            () -> assertThat(result.isValid(), is(false)),
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                is("Display context parameter '#DATETIMEDISPLAY(HHmmss)' is unsupported for field type 'Text' "
                    + "of field 'CASE_FIELD' on tab 'CaseEventToFields'"))
        );
    }

    private static CaseFieldEntity caseFieldEntity() {
        return caseFieldEntity(fieldTypeEntity(FieldTypeUtils.BASE_DATE_TIME));
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
