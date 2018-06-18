package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrderSummaryTypeCannotBeEditableValidationErrorTest {


    private static final String OVERRIDDEN_ERROR_MESSAGE = "The overridden error message";

    @Mock
    private ValidationErrorMessageCreator mockValidationErrorMessageCreator;

    private OrderSummaryTypeCannotBeEditableValidationError classUnderTest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockValidationErrorMessageCreator.createErrorMessage(any(OrderSummaryTypeCannotBeEditableValidationError.class)))
            .thenReturn(OVERRIDDEN_ERROR_MESSAGE);
        classUnderTest = new OrderSummaryTypeCannotBeEditableValidationError(
            eventCaseFieldEntity(caseField("OrderSummaryField"),event("EventId"))
        );
    }

    @Test
    public void testDefaultMessage() {
        assertEquals(
            "'OrderSummaryField' is OrderSummary type and cannot be editable for event with reference 'EventId'",
            classUnderTest.getDefaultMessage()
        );
    }

    @Test
    public void testCreateMessage() {
        assertEquals(OVERRIDDEN_ERROR_MESSAGE, classUnderTest.createMessage(mockValidationErrorMessageCreator));
        verify(mockValidationErrorMessageCreator).createErrorMessage(classUnderTest);
    }

    private EventCaseFieldEntity eventCaseFieldEntity(CaseFieldEntity caseField, EventEntity event) {
        EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
        eventCaseFieldEntity.setCaseField(caseField);
        eventCaseFieldEntity.setEvent(event);
        return eventCaseFieldEntity;
    }

    private CaseFieldEntity caseField(String reference) {
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setReference(reference);
        return caseFieldEntity;
    }

    private EventEntity event(String reference) {
        EventEntity eventEntity = new EventEntity();
        eventEntity.setReference(reference);
        return eventEntity;
    }

}
