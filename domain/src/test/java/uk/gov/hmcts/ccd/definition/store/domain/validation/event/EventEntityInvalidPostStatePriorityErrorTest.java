package uk.gov.hmcts.ccd.definition.store.domain.validation.event;


import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventEntityInvalidPostStatePriorityErrorTest {

    @Mock
    private ValidationErrorMessageCreator mockValidationErrorMessageCreator;

    private EventEntityInvalidPostStatePriorityError classUnderTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockValidationErrorMessageCreator.createErrorMessage(any(EventEntityInvalidPostStatePriorityError.class)))
            .thenReturn("Duplicate Post state");
        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference("TestCaseType");
        classUnderTest = new EventEntityInvalidPostStatePriorityError(
            eventEntityWithReference("createCase"),
            new EventEntityValidationContext(caseType));
    }

    @Test
    void testDefaultMessage() {
        assertEquals(
            "Duplicate post state priorities for case type 'TestCaseType', event 'createCase'",
            classUnderTest.getDefaultMessage()
        );
    }

    @Test
    void testCreateMessage() {
        assertEquals("Duplicate Post state", classUnderTest.createMessage(mockValidationErrorMessageCreator));
        verify(mockValidationErrorMessageCreator).createErrorMessage(classUnderTest);
    }

    private EventEntity eventEntityWithReference(String eventReference) {
        EventEntity eventEntity = new EventEntity();
        eventEntity.setReference(eventReference);
        return eventEntity;
    }
}
