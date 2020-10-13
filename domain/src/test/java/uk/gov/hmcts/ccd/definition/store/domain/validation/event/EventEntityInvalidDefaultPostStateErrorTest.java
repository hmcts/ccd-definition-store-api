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

public class EventEntityInvalidDefaultPostStateErrorTest {

    @Mock
    private ValidationErrorMessageCreator mockValidationErrorMessageCreator;

    private EventEntityInvalidDefaultPostStateError classUnderTest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockValidationErrorMessageCreator.createErrorMessage(any(EventEntityInvalidDefaultPostStateError.class)))
            .thenReturn("Invalid Default Post State");
        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference("TestCaseType");
        classUnderTest = new EventEntityInvalidDefaultPostStateError(
            eventEntityWithReference("createCase"),
            new EventEntityValidationContext(caseType));
    }

    @Test
    public void testDefaultMessage() {
        assertEquals(
            "Non-conditional post state is required for case type 'TestCaseType', event 'createCase'",
            classUnderTest.getDefaultMessage()
        );
    }

    @Test
    public void testCreateMessage() {
        assertEquals("Invalid Default Post State", classUnderTest.createMessage(mockValidationErrorMessageCreator));
        verify(mockValidationErrorMessageCreator).createErrorMessage(classUnderTest);
    }

    private EventEntity eventEntityWithReference(String eventReference) {
        EventEntity eventEntity = new EventEntity();
        eventEntity.setCaseType(createCaseTypeEntity());
        eventEntity.setReference(eventReference);
        return eventEntity;
    }

    private CaseTypeEntity createCaseTypeEntity() {
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference("TestCaseRef");
        return caseTypeEntity;
    }
}
