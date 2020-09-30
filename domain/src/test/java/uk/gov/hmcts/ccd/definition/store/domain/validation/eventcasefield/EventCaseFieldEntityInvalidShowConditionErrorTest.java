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

public class EventCaseFieldEntityInvalidShowConditionErrorTest {

    private static final String OVERRIDDEN_ERROR_MESSAGE = "The overridden error message";

    @Mock
    private ValidationErrorMessageCreator mockValidationErrorMessageCreator;

    private EventCaseFieldEntityInvalidShowConditionError classUnderTest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockValidationErrorMessageCreator.createErrorMessage(
            any(EventCaseFieldEntityInvalidShowConditionError.class)))
            .thenReturn(OVERRIDDEN_ERROR_MESSAGE);
        classUnderTest = new EventCaseFieldEntityInvalidShowConditionError(
            eventCaseFieldEntity("ShowCondition"),
            eventCaseFieldEntityValidationContext("Event Id")
        );
    }

    @Test
    public void testDefaultMessage() {
        assertEquals(
            "Show condition 'ShowCondition' invalid for event 'Event Id'",
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
