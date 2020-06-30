package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;

public class EventCaseFieldOrderSummaryCaseFieldValidatorTest {

    public static final String ORDER_SUMMARY = "OrderSummary";
    private final EventCaseFieldOrderSummaryCaseFieldValidator classUnderTest = new EventCaseFieldOrderSummaryCaseFieldValidator();

    @Test
    public void shouldPassValidationIfFieldIsNotLabelType() {

        assertTrue(classUnderTest.validate(eventCaseFieldEntity(caseField("NotOrderSummary"), null, DisplayContext.MANDATORY), null).isValid());
        assertTrue(classUnderTest.validate(eventCaseFieldEntity(caseField("NotOrderSummary"), null, DisplayContext.OPTIONAL), null).isValid());

    }

    @Test
    public void shouldPassValidationIfFieldTypeMandatory() throws Exception {

        assertTrue(classUnderTest.validate(eventCaseFieldEntity(caseField(ORDER_SUMMARY), null, DisplayContext.MANDATORY), null).isValid());

    }

    @Test
    public void shouldPassValidationIfDisplayContextIsNull() throws Exception {

        ValidationResult validationResult = classUnderTest.validate(eventCaseFieldEntity(caseField(ORDER_SUMMARY), null, null), null);

        assertAll(
            () -> assertTrue(validationResult.isValid()),
            () -> assertEquals(0, validationResult.getValidationErrors().size()));
    }

    @Test
    public void shouldReturnValidationErrorIfFieldTypeOptional() throws Exception {

        ValidationResult validationResult = classUnderTest.validate(eventCaseFieldEntity(caseField(ORDER_SUMMARY),
            event("Event Reference"), DisplayContext.OPTIONAL), null);

        assertAll(
            () -> assertThat(validationResult.isValid(), is(false)),
            () -> assertThat(validationResult.getValidationErrors(), hasSize(1)),
            () -> assertThat(validationResult.getValidationErrors(),
                             hasItem(
                                 hasProperty("defaultMessage",
                                     equalTo("'OrderSummary' is OrderSummary type and "
                                         + "has to be mandatory (not editable but has to be added to a form in UI) "
                                         + "for event with reference 'Event Reference'"))))
        );
    }

    @Test
    public void shouldReturnValidationErrorIfFieldTypeReadOnly() throws Exception {

        ValidationResult validationResult = classUnderTest.validate(eventCaseFieldEntity(caseField(ORDER_SUMMARY),
            event("Event Reference"), DisplayContext.READONLY), null);

        assertAll(
            () -> assertThat(validationResult.isValid(), is(false)),
            () -> assertThat(validationResult.getValidationErrors(), hasSize(1)),
            () -> assertThat(validationResult.getValidationErrors(),
                             hasItem(
                                 hasProperty("defaultMessage",
                                     equalTo("'OrderSummary' is OrderSummary type and has to be mandatory "
                                         + "(not editable but has to be added to a form in UI) for event with reference 'Event Reference'"))))
        );
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

    private EventCaseFieldEntity eventCaseFieldEntity(CaseFieldEntity caseField, EventEntity event, DisplayContext displayContext) {
        EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
        eventCaseFieldEntity.setCaseField(caseField);
        eventCaseFieldEntity.setEvent(event);
        eventCaseFieldEntity.setDisplayContext(displayContext);
        return eventCaseFieldEntity;
    }
}
