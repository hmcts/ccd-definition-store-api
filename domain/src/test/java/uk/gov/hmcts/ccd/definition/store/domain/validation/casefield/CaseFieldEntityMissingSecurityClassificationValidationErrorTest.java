package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;


import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CaseFieldEntityMissingSecurityClassificationValidationErrorTest {

    private static final String OVERRIDDEN_ERROR_MESSAGE = "The overridden error message";

    @Mock
    private ValidationErrorMessageCreator mockValidationErrorMessageCreator;

    private CaseFieldEntityMissingSecurityClassificationValidationError classUnderTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockValidationErrorMessageCreator.createErrorMessage(
            any(CaseFieldEntityMissingSecurityClassificationValidationError.class)))
            .thenReturn(OVERRIDDEN_ERROR_MESSAGE);
        classUnderTest = new CaseFieldEntityMissingSecurityClassificationValidationError(
            caseFieldEntityWithReferenceAndSecurityClassification("Case Field Reference"),
            null
        );
    }

    @Test
    void testDefaultMessage() {
        assertEquals(
            "CaseField with reference 'Case Field Reference' must have a Security Classification defined",
            classUnderTest.getDefaultMessage()
        );
    }

    @Test
    void testCreateMessage() {
        assertEquals(OVERRIDDEN_ERROR_MESSAGE, classUnderTest.createMessage(mockValidationErrorMessageCreator));
        verify(mockValidationErrorMessageCreator).createErrorMessage(classUnderTest);
    }

    private CaseFieldEntity caseFieldEntityWithReferenceAndSecurityClassification(String caseFieldReference) {
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setReference(caseFieldReference);
        return caseFieldEntity;
    }

}
