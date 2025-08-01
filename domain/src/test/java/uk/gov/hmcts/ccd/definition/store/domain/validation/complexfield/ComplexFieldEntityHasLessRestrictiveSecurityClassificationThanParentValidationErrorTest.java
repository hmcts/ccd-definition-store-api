package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;


import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ComplexFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationErrorTest {

    private static final String OVERRIDDEN_ERROR_MESSAGE = "The overridden error message";

    @Mock
    private ValidationErrorMessageCreator mockValidationErrorMessageCreator;

    @Mock
    private CaseFieldComplexFieldEntityValidator.ValidationContext mockComplexFieldEntityValidationContext;

    private ComplexFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError classUnderTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockValidationErrorMessageCreator.createErrorMessage(
            any(ComplexFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError.class)))
            .thenReturn(OVERRIDDEN_ERROR_MESSAGE);
        when(mockComplexFieldEntityValidationContext.getCaseName()).thenReturn("CaseType Name");
        when(mockComplexFieldEntityValidationContext.getCaseFieldReference())
            .thenReturn(("Parent CaseField Reference"));
        when(mockComplexFieldEntityValidationContext.getParentSecurityClassification())
            .thenReturn(SecurityClassification.PRIVATE);
        classUnderTest = new ComplexFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError(
            complexFieldEntityWithReferenceAndSecurityClassification(
                "Complex Field Reference", SecurityClassification.PUBLIC),
            mockComplexFieldEntityValidationContext
        );
    }

    @Test
    void testDefaultMessage() {
        assertEquals(
            "Security classification for ComplexField with reference 'Complex Field Reference' "
                + "has a less restrictive security classification of 'PUBLIC' than its parent CaseField "
                + "'Parent CaseField Reference' which is 'PRIVATE'.",
            classUnderTest.getDefaultMessage()
        );
    }

    @Test
    void testCreateMessage() {
        assertEquals(OVERRIDDEN_ERROR_MESSAGE, classUnderTest.createMessage(mockValidationErrorMessageCreator));
        verify(mockValidationErrorMessageCreator).createErrorMessage(classUnderTest);
    }

    private ComplexFieldEntity complexFieldEntityWithReferenceAndSecurityClassification(
        String caseFieldReference, SecurityClassification securityClassification) {
        ComplexFieldEntity complexFieldEntity = new ComplexFieldEntity();
        complexFieldEntity.setSecurityClassification(securityClassification);
        complexFieldEntity.setReference(caseFieldReference);
        return complexFieldEntity;
    }

}
