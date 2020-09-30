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


public class EventCaseFieldCasePaymentHistoryViewerCaseFieldValidatorTest {

    public static final String CASE_PAYMENT_HISTORY_VIEWER = "CasePaymentHistoryViewer";
    private EventCaseFieldCasePaymentHistoryViewerCaseFieldValidator classUnderTest =
        new EventCaseFieldCasePaymentHistoryViewerCaseFieldValidator();

    @Test
    public void shouldPassValidationIfFieldIsNotCasePaymentHistoryViewer() {

        assertTrue(classUnderTest.validate(eventCaseFieldEntity(caseField("NotCasePaymentHistoryViewer"),
            null, DisplayContext.READONLY), null).isValid());
        assertTrue(classUnderTest.validate(eventCaseFieldEntity(caseField("NotCasePaymentHistoryViewer"),
            null, DisplayContext.OPTIONAL), null).isValid());

    }

    @Test
    public void shouldPassValidationIfFieldTypeReadonly() throws Exception {

        assertTrue(classUnderTest.validate(eventCaseFieldEntity(caseField(CASE_PAYMENT_HISTORY_VIEWER), null,
            DisplayContext.READONLY), null).isValid());

    }

    @Test
    public void shouldPassValidationIfDisplayContextIsNull() throws Exception {

        ValidationResult validationResult = classUnderTest.validate(eventCaseFieldEntity(
            caseField(CASE_PAYMENT_HISTORY_VIEWER), null, null), null);

        assertAll(
            () -> assertTrue(validationResult.isValid()),
            () -> assertEquals(0, validationResult.getValidationErrors().size()));
    }

    @Test
    public void shouldReturnValidationErrorIfFieldTypeOptional() throws Exception {

        ValidationResult validationResult = classUnderTest.validate(
            eventCaseFieldEntity(caseField(CASE_PAYMENT_HISTORY_VIEWER), event("Event Reference"),
            DisplayContext.OPTIONAL), null);

        assertAll(
            () -> assertThat(validationResult.isValid(), is(false)),
            () -> assertThat(validationResult.getValidationErrors(), hasSize(1)),
            () -> assertThat(validationResult.getValidationErrors(),
                hasItem(
                    hasProperty("defaultMessage",
                        equalTo(
                            "'" + CASE_PAYMENT_HISTORY_VIEWER + "' is CasePaymentHistoryViewer type "
                                + "and cannot be editable for event with reference 'Event Reference'"))))
        );
    }

    @Test
    public void shouldReturnValidationErrorIfFieldTypeMandatory() throws Exception {

        ValidationResult validationResult = classUnderTest.validate(eventCaseFieldEntity(
            caseField(CASE_PAYMENT_HISTORY_VIEWER),
            event("Event Reference"),
            DisplayContext.MANDATORY), null);

        assertAll(
            () -> assertThat(validationResult.isValid(), is(false)),
            () -> assertThat(validationResult.getValidationErrors(), hasSize(1)),
            () -> assertThat(validationResult.getValidationErrors(),
                hasItem(
                    hasProperty("defaultMessage",
                        equalTo("'" + CASE_PAYMENT_HISTORY_VIEWER + "' is CasePaymentHistoryViewer type "
                            + "and cannot be editable for event with reference 'Event Reference'"))))
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
