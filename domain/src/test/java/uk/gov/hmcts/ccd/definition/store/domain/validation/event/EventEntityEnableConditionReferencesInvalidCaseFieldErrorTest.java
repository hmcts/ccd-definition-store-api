package uk.gov.hmcts.ccd.definition.store.domain.validation.event;


import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventEntityEnableConditionReferencesInvalidCaseFieldErrorTest {

    private static final String OVERRIDDEN_ERROR_MESSAGE = "The overridden error message";

    @Mock
    private ValidationErrorMessageCreator mockValidationErrorMessageCreator;

    private EventEntityEnableConditionReferencesInvalidCaseFieldError classUnderTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockValidationErrorMessageCreator.createErrorMessage(
            any(EventEntityEnableConditionReferencesInvalidCaseFieldError.class)))
            .thenReturn(OVERRIDDEN_ERROR_MESSAGE);
        classUnderTest = new EventEntityEnableConditionReferencesInvalidCaseFieldError("TextField",
            eventEntityWithReference("Event Reference"),
            "TextFieldA='Test'"
        );
    }

    @Test
    void testDefaultMessage() {
        assertEquals(
            "Unknown field 'TextField' for event 'Event Reference' in event enabling condition: 'TextFieldA='Test''",
            classUnderTest.getDefaultMessage()
        );
    }

    @Test
    void testCreateMessage() {
        assertEquals(OVERRIDDEN_ERROR_MESSAGE, classUnderTest.createMessage(mockValidationErrorMessageCreator));
        verify(mockValidationErrorMessageCreator).createErrorMessage(classUnderTest);
    }

    private EventEntity eventEntityWithReference(String eventReference) {
        EventEntity eventEntity = new EventEntity();
        eventEntity.setReference(eventReference);
        return eventEntity;
    }
}
