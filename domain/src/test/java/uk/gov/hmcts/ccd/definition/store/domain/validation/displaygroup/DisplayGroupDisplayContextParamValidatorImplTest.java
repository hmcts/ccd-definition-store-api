package uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DisplayGroupDisplayContextParamValidatorImplTest {

    @Test
    void shouldNotFireValidationErrorWhenNoDisplayContextParameterExists() {
        DisplayGroupCaseFieldEntity entity = dpEntity();
        ValidationResult validationResult
            = new DisplayGroupDisplayContextParamValidator().validate(entity);

        assertTrue(validationResult.isValid());
    }

    @Test
    void shouldNotFireValidationErrorWhenDateTimeDisplayContextParameter() {
        DisplayGroupCaseFieldEntity entity = dpEntityFailureCase("#DATETIMEENTRY(HHmmss)");
        ValidationResult validationResult
            = new DisplayGroupDisplayContextParamValidator().validate(entity);

        assertTrue(validationResult.isValid());
    }

    @Test
    void shouldNotFireValidationErrorWhenArgumentDisplayContextParameter() {
        DisplayGroupCaseFieldEntity entity = dpEntityFailureCase("#ARGUMENT(TEST ARGUMENT)");
        ValidationResult validationResult
            = new DisplayGroupDisplayContextParamValidator().validate(entity);

        assertTrue(validationResult.isValid());
    }

    @Test
    void shouldFireValidationErrorDisplayContextParamHasValueNotPresentInCollection() {
        DisplayGroupCaseFieldEntity entity = dpEntityFailureCase("#TABLE(firstname)");
        ValidationResult validationResult
            = new DisplayGroupDisplayContextParamValidator().validate(entity);

        assertFalse(validationResult.isValid());
        assertEquals(2, validationResult.getValidationErrors().size());
        assertEquals("Display context parameter is not of type collection",
            validationResult.getValidationErrors().get(0).getDefaultMessage());
        assertEquals("ListCodeElement firstname display context parameter is not one of the fields in collection",
            validationResult.getValidationErrors().get(1).getDefaultMessage());

        entity = dpEntityFailureCase("#LIST(firstname)");
        validationResult
            = new DisplayGroupDisplayContextParamValidator().validate(entity);

        assertFalse(validationResult.isValid());
        assertEquals(2, validationResult.getValidationErrors().size());
        assertEquals("Display context parameter is not of type collection",
            validationResult.getValidationErrors().get(0).getDefaultMessage());
        assertEquals("ListCodeElement firstname display context parameter is not one of the fields in collection",
            validationResult.getValidationErrors().get(1).getDefaultMessage());
    }

    @Test
    void shouldFireValidationErrorWhenDisplayContextParamFormatIncorrect() {
        DisplayGroupCaseFieldEntity entity = dpEntityFailureCase("#sss(firstname)");
        ValidationResult validationResult
            = new DisplayGroupDisplayContextParamValidator().validate(entity);

        assertFalse(validationResult.isValid());
        assertEquals(2, validationResult.getValidationErrors().size());
        assertEquals("Display context parameter is not of type collection",
            validationResult.getValidationErrors().get(0).getDefaultMessage());
        assertEquals("DisplayContextParameter text should begin with "
                + "#LIST(, #TABLE(, #DATETIMEENTRY(, #DATETIMEDISPLAY( or #ARGUMENT(",
            validationResult.getValidationErrors().get(1).getDefaultMessage());
    }

    private DisplayGroupCaseFieldEntity dpEntity() {

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
        DisplayGroupCaseFieldEntity entity = new DisplayGroupCaseFieldEntity();
        entity.setCaseField(caseFieldEntity);
        EventEntity eventEntity = new EventEntity();
        eventEntity.setReference("Event1");
        return entity;
    }

    private DisplayGroupCaseFieldEntity dpEntityFailureCase(final String displayContextParameter) {

        DisplayGroupCaseFieldEntity entity = new DisplayGroupCaseFieldEntity();
        entity.setDisplayContextParameter(displayContextParameter);
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
        entity.setCaseField(caseFieldEntity);
        EventEntity eventEntity = new EventEntity();
        eventEntity.setReference("Event1");
        return entity;
    }
}
