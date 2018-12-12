package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.*;

public class EventCaseFieldDisplayContextParameterValidatorImplTest {

    @Test
    public void shouldNotFireValidationErrorWhenNoDisplayContextParameterExists() {
        EventCaseFieldEntity eventCaseFieldEntity = eventCaseFieldEntity();
        ValidationResult validationResult
            = new EventCaseFieldDisplayContextParameterValidatorImpl().validate(eventCaseFieldEntity, null);

        assertTrue(validationResult.isValid());
    }

    @Test
    public void shouldFireValidationErrorDisplayContextParamHasValueNotPresentInCollection() {
        EventCaseFieldEntity eventCaseFieldEntity = eventCaseFieldEntityFailureCase();
        eventCaseFieldEntity.setDisplayContext(DisplayContext.OPTIONAL);
        ValidationResult validationResult
            = new EventCaseFieldDisplayContextParameterValidatorImpl().validate(eventCaseFieldEntity, null);

        assertFalse(validationResult.isValid());
        assertEquals(2, validationResult.getValidationErrors().size());
        assertEquals("Display context parameter is not of type collection",validationResult.getValidationErrors().get(0).getDefaultMessage());
        assertEquals("ListCodeElement firstname display context parameter is not one of the fields in collection",validationResult.getValidationErrors().get(1).getDefaultMessage());
    }

    private EventCaseFieldEntity eventCaseFieldEntity() {

        EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setReference("Case1");
        FieldTypeEntity fieldType = new FieldTypeEntity();
        FieldTypeEntity collectionFieldType = new FieldTypeEntity();
        List<ComplexFieldEntity> complexFields = new ArrayList<>();
        ComplexFieldEntity complexFieldEntity = new ComplexFieldEntity();
        complexFieldEntity.setReference("title");
        complexFields.add(complexFieldEntity);
        collectionFieldType.addComplexFields(complexFields);
        fieldType.setCollectionFieldType(collectionFieldType);
        caseFieldEntity.setFieldType(fieldType);
        eventCaseFieldEntity.setCaseField(caseFieldEntity);
        EventEntity eventEntity = new EventEntity();
        eventEntity.setReference("Event1");
        eventCaseFieldEntity.setEvent(eventEntity);
        return eventCaseFieldEntity;
    }

    private EventCaseFieldEntity eventCaseFieldEntityFailureCase() {

        EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
        eventCaseFieldEntity.setDisplayContextParameter("#TABLE(firstname)");
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setReference("Case1");
        FieldTypeEntity fieldType = new FieldTypeEntity();
        FieldTypeEntity collectionFieldType = new FieldTypeEntity();
        List<ComplexFieldEntity> complexFields = new ArrayList<>();
        ComplexFieldEntity complexFieldEntity = new ComplexFieldEntity();
        complexFieldEntity.setReference("title");
        complexFields.add(complexFieldEntity);
        collectionFieldType.addComplexFields(complexFields);
        fieldType.setCollectionFieldType(collectionFieldType);
        caseFieldEntity.setFieldType(fieldType);
        eventCaseFieldEntity.setCaseField(caseFieldEntity);
        EventEntity eventEntity = new EventEntity();
        eventEntity.setReference("Event1");
        eventCaseFieldEntity.setEvent(eventEntity);
        return eventCaseFieldEntity;
    }
}
