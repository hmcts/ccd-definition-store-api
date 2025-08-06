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

class EventEntityInvalidDefaultPostStateErrorTest {

    @Mock
    private ValidationErrorMessageCreator mockValidationErrorMessageCreator;

    private EventEntityInvalidDefaultPostStateError classUnderTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockValidationErrorMessageCreator.createErrorMessage(any(EventEntityInvalidDefaultPostStateError.class)))
            .thenReturn("Invalid Default Post State");
        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference("TestCaseType");
        classUnderTest = new EventEntityInvalidDefaultPostStateError(
            eventEntityWithReference("createCase"),
            new EventEntityValidationContext(caseType));
    }

    @Test
    void testDefaultMessage() {
        assertEquals(
            "Non-conditional post state is required for case type 'TestCaseType', event 'createCase'",
            classUnderTest.getDefaultMessage()
        );
    }

    @Test
    void testCreateMessage() {
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
