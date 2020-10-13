package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldErrorTest {

    private static final String OVERRIDDEN_ERROR_MESSAGE = "The overridden error message";

    @Mock
    private ValidationErrorMessageCreator mockValidationErrorMessageCreator;

    private EventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError classUnderTest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockValidationErrorMessageCreator.createErrorMessage(
            any(EventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError.class)))
            .thenReturn(OVERRIDDEN_ERROR_MESSAGE);
        classUnderTest = new EventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError(
            "ShowCondition Field Value",
            eventCaseFieldEntityValidationContext("Event Id"),
            eventCaseFieldEntity("ShowCondition")
        );
    }

    @Test
    public void testDefaultMessage() {
        assertEquals(
            "Unknown field 'ShowCondition Field Value' for event 'Event Id' in show condition: 'ShowCondition'",
            classUnderTest.getDefaultMessage()
        );
    }

    @Test
    public void testCreateMessage() {
        assertEquals(OVERRIDDEN_ERROR_MESSAGE, classUnderTest.createMessage(mockValidationErrorMessageCreator));
        verify(mockValidationErrorMessageCreator).createErrorMessage(classUnderTest);
    }

    private EventCaseFieldEntity eventCaseFieldEntity(String showCondition) {
        EventCaseFieldEntity eventCaseFieldEntity = new EventCaseFieldEntity();
        eventCaseFieldEntity.setShowCondition(showCondition);
        return eventCaseFieldEntity;
    }

    private EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext(String eventId) {
        EventCaseFieldEntityValidationContext caseFieldEntityValidationContext =
            mock(EventCaseFieldEntityValidationContext.class);
        when(caseFieldEntityValidationContext.getEventId()).thenReturn(eventId);
        return caseFieldEntityValidationContext;
    }

}
