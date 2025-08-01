package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventCaseFieldDisplayContextParameterValidatorImplTest {

    @Test
    void shouldNotFireValidationErrorWhenNoDisplayContextParameterExists() {
        EventCaseFieldEntity eventCaseFieldEntity = eventCaseFieldEntity();
        ValidationResult validationResult
            = new EventCaseFieldDisplayContextParameterValidatorImpl().validate(eventCaseFieldEntity, null);

        assertTrue(validationResult.isValid());
    }

    @Test
    void shouldNotFireValidationErrorWhenDateTimeDisplayContextParameter() {
        EventCaseFieldEntity eventCaseFieldEntity = eventCaseFieldEntityFailureCase("#DATETIMEENTRY(HHmmss)");
        ValidationResult validationResult
            = new EventCaseFieldDisplayContextParameterValidatorImpl().validate(eventCaseFieldEntity, null);

        assertTrue(validationResult.isValid());
    }

    @Test
    void shouldNotFireValidationErrorWhenArgumentDisplayContextParameter() {
        EventCaseFieldEntity eventCaseFieldEntity = eventCaseFieldEntityFailureCase("#ARGUMENT(testArgument)");
        ValidationResult validationResult
            = new EventCaseFieldDisplayContextParameterValidatorImpl().validate(eventCaseFieldEntity, null);

        assertTrue(validationResult.isValid());
    }

    @Test
    void shouldFireValidationErrorDisplayContextParamHasValueNotPresentInCollection() {
        EventCaseFieldEntity eventCaseFieldEntity = eventCaseFieldEntityFailureCase("#TABLE(firstname)");
        eventCaseFieldEntity.setDisplayContext(DisplayContext.OPTIONAL);
        ValidationResult validationResult
            = new EventCaseFieldDisplayContextParameterValidatorImpl().validate(eventCaseFieldEntity, null);

        assertFalse(validationResult.isValid());
        assertEquals(2, validationResult.getValidationErrors().size());
        assertEquals("Display context parameter is not of type collection",
            validationResult.getValidationErrors().get(0).getDefaultMessage());
        assertEquals("ListCodeElement firstname display context parameter is not one of the fields in collection",
            validationResult.getValidationErrors().get(1).getDefaultMessage());

        eventCaseFieldEntity = eventCaseFieldEntityFailureCase("#LIST(firstname)");
        eventCaseFieldEntity.setDisplayContext(DisplayContext.OPTIONAL);
        validationResult
            = new EventCaseFieldDisplayContextParameterValidatorImpl().validate(eventCaseFieldEntity, null);

        assertFalse(validationResult.isValid());
        assertEquals(2, validationResult.getValidationErrors().size());
        assertEquals("Display context parameter is not of type collection",
            validationResult.getValidationErrors().get(0).getDefaultMessage());
        assertEquals("ListCodeElement firstname display context parameter is not one of the fields in collection",
            validationResult.getValidationErrors().get(1).getDefaultMessage());
    }

    @Test
    void shouldFireValidationErrorWhenDisplayContextParamFormatIncorrect() {
        EventCaseFieldEntity eventCaseFieldEntity = eventCaseFieldEntityFailureCase("#sss(firstname)");
        eventCaseFieldEntity.setDisplayContext(DisplayContext.OPTIONAL);
        ValidationResult validationResult
            = new EventCaseFieldDisplayContextParameterValidatorImpl().validate(eventCaseFieldEntity, null);

        assertFalse(validationResult.isValid());
        assertEquals(2, validationResult.getValidationErrors().size());
        assertEquals("Display context parameter is not of type collection",
            validationResult.getValidationErrors().get(0).getDefaultMessage());
        assertEquals("DisplayContextParameter text should begin with #LIST( or #TABLE(",
            validationResult.getValidationErrors().get(1).getDefaultMessage());
    }

    private EventCaseFieldEntity eventCaseFieldEntity() {

        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setReference("Case1");
        FieldTypeEntity collectionFieldType = new FieldTypeEntity();
        List<ComplexFieldEntity> complexFields = new ArrayList<>();
        ComplexFieldEntity complexFieldEntity = new ComplexFieldEntity();
        complexFieldEntity.setReference("title");
        complexFields.add(complexFieldEntity);
        collectionFieldType.addComplexFields(complexFields);
        FieldTypeEntity fieldType = new FieldTypeEntity();
        fieldType.setCollectionFieldType(collectionFieldType);
        caseFieldEntity.setFieldType(fieldType);
        EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
        eventCaseFieldEntity.setCaseField(caseFieldEntity);
        EventEntity eventEntity = new EventEntity();
        eventEntity.setReference("Event1");
        eventCaseFieldEntity.setEvent(eventEntity);
        return eventCaseFieldEntity;
    }

    private EventCaseFieldEntity eventCaseFieldEntityFailureCase(final String displayContextParameter) {

        EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
        eventCaseFieldEntity.setDisplayContextParameter(displayContextParameter);
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setReference("Case1");
        FieldTypeEntity collectionFieldType = new FieldTypeEntity();
        List<ComplexFieldEntity> complexFields = new ArrayList<>();
        ComplexFieldEntity complexFieldEntity = new ComplexFieldEntity();
        complexFieldEntity.setReference("title");
        complexFields.add(complexFieldEntity);
        collectionFieldType.addComplexFields(complexFields);
        FieldTypeEntity fieldType = new FieldTypeEntity();
        fieldType.setCollectionFieldType(collectionFieldType);
        caseFieldEntity.setFieldType(fieldType);
        eventCaseFieldEntity.setCaseField(caseFieldEntity);
        EventEntity eventEntity = new EventEntity();
        eventEntity.setReference("Event1");
        eventCaseFieldEntity.setEvent(eventEntity);
        return eventCaseFieldEntity;
    }
}
