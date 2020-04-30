package uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.*;

public class DisplayGroupDisplayContextParamValidatorImplTest {

    @Test
    public void shouldNotFireValidationErrorWhenNoDisplayContextParameterExists() {
        DisplayGroupCaseFieldEntity entity = dpEntity();
        ValidationResult validationResult
            = new DisplayGroupDisplayContextParamValidator().validate(entity);

        assertTrue(validationResult.isValid());
    }

    @Test
    public void shouldNotFireValidationErrorWhenDateTimeDisplayContextParameter() {
        DisplayGroupCaseFieldEntity entity = dpEntityFailureCase("#DATETIMEENTRY(HHmmss)");
        ValidationResult validationResult
            = new DisplayGroupDisplayContextParamValidator().validate(entity);

        assertTrue(validationResult.isValid());
    }

    @Test
    public void shouldFireValidationErrorDisplayContextParamHasValueNotPresentInCollection() {
        DisplayGroupCaseFieldEntity entity = dpEntityFailureCase("#TABLE(firstname)");
        ValidationResult validationResult
            = new DisplayGroupDisplayContextParamValidator().validate(entity);

        assertFalse(validationResult.isValid());
        assertEquals(2, validationResult.getValidationErrors().size());
        assertEquals("Display context parameter is not of type collection", validationResult.getValidationErrors().get(0).getDefaultMessage());
        assertEquals("ListCodeElement firstname display context parameter is not one of the fields in collection",
            validationResult.getValidationErrors().get(1).getDefaultMessage());

        entity = dpEntityFailureCase("#LIST(firstname)");
        validationResult
            = new DisplayGroupDisplayContextParamValidator().validate(entity);

        assertFalse(validationResult.isValid());
        assertEquals(2, validationResult.getValidationErrors().size());
        assertEquals("Display context parameter is not of type collection", validationResult.getValidationErrors().get(0).getDefaultMessage());
        assertEquals("ListCodeElement firstname display context parameter is not one of the fields in collection",
            validationResult.getValidationErrors().get(1).getDefaultMessage());
    }

    @Test
    public void shouldFireValidationErrorWhenDisplayContextParamFormatIncorrect() {
        DisplayGroupCaseFieldEntity entity = dpEntityFailureCase("#sss(firstname)");
        ValidationResult validationResult
            = new DisplayGroupDisplayContextParamValidator().validate(entity);

        assertFalse(validationResult.isValid());
        assertEquals(2, validationResult.getValidationErrors().size());
        assertEquals("Display context parameter is not of type collection", validationResult.getValidationErrors().get(0).getDefaultMessage());
        assertEquals("DisplayContextParameter text should begin with #LIST(, #TABLE(, #DATETIMEENTRY( or #DATETIMEDISPLAY(",
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
