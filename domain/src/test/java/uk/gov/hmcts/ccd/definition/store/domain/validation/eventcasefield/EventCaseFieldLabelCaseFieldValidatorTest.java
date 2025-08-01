package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class EventCaseFieldLabelCaseFieldValidatorTest {

    private EventCaseFieldLabelCaseFieldValidator classUnderTest =
        new EventCaseFieldLabelCaseFieldValidator();

    @Test
    void fieldIsNotLabelType_validationPassesRegardlessOfFieldSetting() {

        assertTrue(classUnderTest.validate(eventCaseFieldEntity(
            caseField("NotLabel"), null, DisplayContext.READONLY), null)
            .isValid());
        assertTrue(classUnderTest.validate(eventCaseFieldEntity(
            caseField("NotLabel"), null, DisplayContext.OPTIONAL), null)
            .isValid());

    }

    @Test
    void fieldIsLabelType_validationPassesWhenFieldIsReadOnly() {

        assertTrue(classUnderTest.validate(eventCaseFieldEntity(
            caseField("Label"), null, DisplayContext.READONLY), null)
            .isValid());

    }

    @Test
    void fieldIsLabelType_validationFailsWithLabelTypeCannotBeEditableValidationErrorWhenFieldIsNotReadOnly() {

        EventCaseFieldEntity eventCaseFieldEntity = eventCaseFieldEntity(
            caseField("Label"), event("Event Reference"), DisplayContext.OPTIONAL);

        ValidationResult validationResult
            = classUnderTest.validate(eventCaseFieldEntity, null);

        assertFalse(validationResult.isValid());
        assertEquals(1, validationResult.getValidationErrors().size());
        ValidationError validationError = validationResult.getValidationErrors().get(0);
        assertThat(validationResult.getValidationErrors(),
            hasItem(
                hasProperty("defaultMessage", equalTo(
                    "'Label' is Label type and cannot be editable for event with reference 'Event Reference'"))
            ));

    }

    @Test
    void fieldIsLabelType_validationDoesNotFailWhenDisplayContextIsNull() {

        EventCaseFieldEntity eventCaseFieldEntity = eventCaseFieldEntity(
            caseField("Label"), event("Event Reference"), null);

        ValidationResult validationResult
            = classUnderTest.validate(eventCaseFieldEntity, null);

        assertTrue(validationResult.isValid());
        assertEquals(0, validationResult.getValidationErrors().size());
    }

    private CaseFieldEntity caseField(String fieldType) {
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(fieldType);
        caseFieldEntity.setFieldType(fieldTypeEntity);
        caseFieldEntity.setReference(fieldType);
        return caseFieldEntity;
    }

    private EventEntity event(String eventReference) {
        EventEntity eventEntity = new EventEntity();
        eventEntity.setReference(eventReference);
        return eventEntity;
    }

    private EventCaseFieldEntity eventCaseFieldEntity(CaseFieldEntity caseField,
                                                      EventEntity event,
                                                      DisplayContext displayContext) {
        EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
        eventCaseFieldEntity.setCaseField(caseField);
        eventCaseFieldEntity.setEvent(event);
        eventCaseFieldEntity.setDisplayContext(displayContext);
        return eventCaseFieldEntity;
    }
}
