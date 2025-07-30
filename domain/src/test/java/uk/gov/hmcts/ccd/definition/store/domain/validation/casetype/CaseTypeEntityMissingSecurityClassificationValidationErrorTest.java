package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;


import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CaseTypeEntityMissingSecurityClassificationValidationErrorTest {

    private static final String OVERRIDDEN_ERROR_MESSAGE = "The overridden error message";

    @Mock
    private ValidationErrorMessageCreator mockValidationErrorMessageCreator;

    private CaseTypeEntityMissingSecurityClassificationValidationError classUnderTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockValidationErrorMessageCreator.createErrorMessage(
            any(CaseTypeEntityMissingSecurityClassificationValidationError.class)))
            .thenReturn(OVERRIDDEN_ERROR_MESSAGE);
        classUnderTest = new CaseTypeEntityMissingSecurityClassificationValidationError(
            caseTypeEntityWithName("Charley says Dont talk to strangers"));
    }

    @Test
    void testDefaultMessage() {
        assertEquals("Case Type with name 'Charley says Dont talk to strangers' "
            + "must have a Security Classification defined", classUnderTest.getDefaultMessage());
    }

    @Test
    void testCreateMessage() {
        assertEquals(OVERRIDDEN_ERROR_MESSAGE, classUnderTest.createMessage(mockValidationErrorMessageCreator));
        verify(mockValidationErrorMessageCreator).createErrorMessage(classUnderTest);
    }

    private CaseTypeEntity caseTypeEntityWithName(String name) {
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setName(name);
        return caseTypeEntity;
    }

}
