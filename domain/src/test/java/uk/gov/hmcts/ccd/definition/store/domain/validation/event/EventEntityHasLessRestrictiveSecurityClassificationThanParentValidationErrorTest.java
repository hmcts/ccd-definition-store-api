package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EventEntityHasLessRestrictiveSecurityClassificationThanParentValidationErrorTest {

    private static final String OVERRIDDEN_ERROR_MESSAGE = "The overridden error message";

    @Mock
    private ValidationErrorMessageCreator mockValidationErrorMessageCreator;

    private EventEntityHasLessRestrictiveSecurityClassificationThanParentValidationError classUnderTest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockValidationErrorMessageCreator.createErrorMessage(any(EventEntityHasLessRestrictiveSecurityClassificationThanParentValidationError.class)))
            .thenReturn(OVERRIDDEN_ERROR_MESSAGE);
        classUnderTest = new EventEntityHasLessRestrictiveSecurityClassificationThanParentValidationError(
            eventEntityWithReferenceAndSecurityClassification("Event Reference", SecurityClassification.PUBLIC),
            eventEntityValidationContext("Parent Case Name", SecurityClassification.PRIVATE)
        );
    }

    @Test
    public void testDefaultMessage() {
        assertEquals(
            "Security classification for Event with reference 'Event Reference' "
                + "has a less restrictive security classification of 'PUBLIC' than its parent CaseType 'Parent Case Name' "
                + "which is 'PRIVATE'.",
            classUnderTest.getDefaultMessage()
        );
    }

    @Test
    public void testCreateMessage() {
        assertEquals(OVERRIDDEN_ERROR_MESSAGE, classUnderTest.createMessage(mockValidationErrorMessageCreator));
        verify(mockValidationErrorMessageCreator).createErrorMessage(classUnderTest);
    }

    private EventEntity eventEntityWithReferenceAndSecurityClassification(String eventEntityReference,
                                                                          SecurityClassification securityClassification) {
        EventEntity eventEntity = new EventEntity();
        eventEntity.setSecurityClassification(securityClassification);
        eventEntity.setReference(eventEntityReference);
        return eventEntity;
    }

    private EventEntityValidationContext eventEntityValidationContext(String parentCaseName, SecurityClassification parentSecurityClassification) {
        EventEntityValidationContext eventEntityValidationContext = mock(EventEntityValidationContext.class);
        when(eventEntityValidationContext.getCaseName()).thenReturn(parentCaseName);
        when(eventEntityValidationContext.getParentSecurityClassification()).thenReturn(parentSecurityClassification);
        return eventEntityValidationContext;
    }

}
