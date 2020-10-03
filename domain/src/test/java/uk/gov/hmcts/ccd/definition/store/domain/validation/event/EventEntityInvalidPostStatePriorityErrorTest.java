package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EventEntityInvalidPostStatePriorityErrorTest {

    @Mock
    private ValidationErrorMessageCreator mockValidationErrorMessageCreator;

    private EventEntityInvalidPostStatePriorityError classUnderTest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockValidationErrorMessageCreator.createErrorMessage(any(EventEntityInvalidPostStatePriorityError.class)))
            .thenReturn("Duplicate Post state");
        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference("TestCaseType");
        classUnderTest = new EventEntityInvalidPostStatePriorityError(
            eventEntityWithReference("createCase"),
            new EventEntityValidationContext(caseType));
    }

    @Test
    public void testDefaultMessage() {
        assertEquals(
            "Duplicate post state priorities for case type 'TestCaseType', event 'createCase'",
            classUnderTest.getDefaultMessage()
        );
    }

    @Test
    public void testCreateMessage() {
        assertEquals("Duplicate Post state", classUnderTest.createMessage(mockValidationErrorMessageCreator));
        verify(mockValidationErrorMessageCreator).createErrorMessage(classUnderTest);
    }

    private EventEntity eventEntityWithReference(String eventReference) {
        EventEntity eventEntity = new EventEntity();
        eventEntity.setReference(eventReference);
        return eventEntity;
    }
}
