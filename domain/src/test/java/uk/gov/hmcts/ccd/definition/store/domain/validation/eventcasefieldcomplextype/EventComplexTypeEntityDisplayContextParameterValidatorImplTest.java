package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefieldcomplextype;

import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.datetime.InvalidDateTimeFormatException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.DisplayContextParameterValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.DisplayContextParameterValidatorFactory;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.*;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class EventComplexTypeEntityDisplayContextParameterValidatorImplTest {

    private EventComplexTypeEntityValidator validator;

    @Mock
    private DisplayContextParameterValidatorFactory displayContextParameterValidatorFactory;

    @Mock
    private DisplayContextParameterValidator displayContextParameterValidator;

    @Mock
    private EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new EventComplexTypeEntityDisplayContextParameterValidatorImpl(displayContextParameterValidatorFactory);
        when(displayContextParameterValidatorFactory.getValidator(Mockito.any())).thenReturn(displayContextParameterValidator);
    }

    @Test
    void shouldValidateEntityWithNoDisplayContextParameter() {
        EventComplexTypeEntity entity = eventComplexTypeEntity("CASE_FIELD", FieldTypeUtils.BASE_TEXT);

        final ValidationResult result = validator.validate(entity, eventCaseFieldEntityValidationContext);

        assertAll(
            () -> assertThat(result.isValid(), is(true))
        );
    }

    @Test
    void shouldValidateEntityWithDateTimeEntryDisplayContextParameter() {
        EventComplexTypeEntity entity = eventComplexTypeEntity("CASE_FIELD", FieldTypeUtils.BASE_DATE_TIME);
        entity.setDisplayContextParameter("#DATETIMEENTRY(hhmmss)");

        final ValidationResult result = validator.validate(entity, eventCaseFieldEntityValidationContext);

        assertAll(
            () -> assertThat(result.isValid(), is(true))
        );
    }

    @Test
    void shouldValidateEntityWithDateTimeDisplayDisplayContextParameter() {
        EventComplexTypeEntity entity = eventComplexTypeEntity("CASE_FIELD", FieldTypeUtils.BASE_DATE_TIME);
        entity.setDisplayContextParameter("#DATETIMEDISPLAY(hhmmss)");

        final ValidationResult result = validator.validate(entity, eventCaseFieldEntityValidationContext);

        assertAll(
            () -> assertThat(result.isValid(), is(true))
        );
    }

    @Test
    void shouldValidateEntityWithDisplayContextParameterForDateFieldType() {
        EventComplexTypeEntity entity = eventComplexTypeEntity("CASE_FIELD", FieldTypeUtils.BASE_DATE);
        entity.setDisplayContextParameter("#DATETIMEDISPLAY(hhmmss)");

        final ValidationResult result = validator.validate(entity, eventCaseFieldEntityValidationContext);

        assertAll(
            () -> assertThat(result.isValid(), is(true))
        );
    }

    @Test
    void shouldFailValidationForInvalidDateTimeFormat() throws Exception {
        EventComplexTypeEntity entity = eventComplexTypeEntity("CASE_FIELD", FieldTypeUtils.BASE_DATE_TIME);
        entity.setDisplayContextParameter("#DATETIMEENTRY(0123456789)");
        doThrow(InvalidDateTimeFormatException.class).when(displayContextParameterValidator).validate(Mockito.any());

        final ValidationResult result = validator.validate(entity, eventCaseFieldEntityValidationContext);

        assertAll(
            () -> assertThat(result.isValid(), is(false)),
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is("Display context parameter '#DATETIMEENTRY(0123456789)' has been incorrectly configured or is invalid for field 'CASE_FIELD'"))
        );
    }

    @Test
    void shouldFailValidationForInvalidDisplayContextParameterType() {
        EventComplexTypeEntity entity = eventComplexTypeEntity("CASE_FIELD", FieldTypeUtils.BASE_DATE_TIME);
        entity.setDisplayContextParameter("#INVALIDPARAMETER(hhmmss)");

        final ValidationResult result = validator.validate(entity, eventCaseFieldEntityValidationContext);

        assertAll(
            () -> assertThat(result.isValid(), is(false)),
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is("Unsupported display context parameter type '#INVALIDPARAMETER(hhmmss)' for field 'CASE_FIELD'"))
        );
    }

    @Test
    void shouldFailValidationForNotAllowedDisplayContextParameterType() {
        EventComplexTypeEntity entity = eventComplexTypeEntity("CASE_FIELD", FieldTypeUtils.BASE_DATE_TIME);
        entity.setDisplayContextParameter("#TABLE(FieldId)");

        final ValidationResult result = validator.validate(entity, eventCaseFieldEntityValidationContext);

        assertAll(
            () -> assertThat(result.isValid(), is(false)),
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is("Unsupported display context parameter type '#TABLE(FieldId)' for field 'CASE_FIELD'"))
        );
    }

//    @Test
//    void shouldFailValidationForNotAllowedFieldType() {
//        EventComplexTypeEntity entity = eventComplexTypeEntity("CASE_FIELD", FieldTypeUtils.BASE_TEXT);
//        entity.setDisplayContextParameter("#DATETIMEDISPLAY(hhmmss)");
//
//        final ValidationResult result = validator.validate(entity, eventCaseFieldEntityValidationContext);
//
//        assertAll(
//            () -> assertThat(result.isValid(), is(false)),
//            () -> assertThat(result.getValidationErrors().size(), is(1)),
//            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is("Display context parameter '#DATETIMEDISPLAY(hhmmss)' is unsupported for field type 'Text' of field 'CASE_FIELD'"))
//        );
//    }

    private EventComplexTypeEntity eventComplexTypeEntity(String reference) {
        EventComplexTypeEntity eventComplexTypeEntity = new EventComplexTypeEntity();
        eventComplexTypeEntity.setReference(reference);
        return eventComplexTypeEntity;
    }

    private EventComplexTypeEntity eventComplexTypeEntity(String caseFieldReference, String caseFieldType) {
        EventComplexTypeEntity eventComplexTypeEntity = eventComplexTypeEntity("Reference");
        eventComplexTypeEntity.setComplexFieldType(
            eventCaseFieldEntity(
                caseFieldEntity(caseFieldReference, fieldTypeEntity(caseFieldType)),
                null
            )
        );
        return eventComplexTypeEntity;
    }

    private static EventCaseFieldEntity eventCaseFieldEntity(CaseFieldEntity caseFieldEntity,
                                                             List<EventComplexTypeEntity> eventComplexTypeEntities) {
        EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
        eventCaseFieldEntity.setEvent(new EventEntity());
        eventCaseFieldEntity.setCaseField(caseFieldEntity);
        eventCaseFieldEntity.addComplexFields(eventComplexTypeEntities);
        return eventCaseFieldEntity;
    }

    private static CaseFieldEntity caseFieldEntity(String reference,
                                                   FieldTypeEntity fieldTypeEntity) {
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setReference(reference);
        caseFieldEntity.setFieldType(fieldTypeEntity);
        return caseFieldEntity;
    }

    private static FieldTypeEntity fieldTypeEntity(String fieldType) {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(fieldType);
        return fieldTypeEntity;
    }
}
