package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;


import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CaseFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationErrorTest {

    private static final String OVERRIDDEN_ERROR_MESSAGE = "The overridden error message";

    @Mock
    private ValidationErrorMessageCreator mockValidationErrorMessageCreator;

    private CaseFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError classUnderTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockValidationErrorMessageCreator.createErrorMessage(
            any(CaseFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError.class)))
            .thenReturn(OVERRIDDEN_ERROR_MESSAGE);
        classUnderTest = new CaseFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError(
            caseFieldEntityWithReferenceAndSecurityClassification(
                "Case Field Reference", SecurityClassification.PUBLIC),
            caseFieldEntityValidationContext("Parent Case Name", SecurityClassification.PRIVATE)
        );
    }

    @Test
    void testDefaultMessage() {
        assertEquals(
            "Security classification for CaseField with reference 'Case Field Reference' "
                + "has a less restrictive security classification of 'PUBLIC' than"
                + " its parent CaseType 'Parent Case Name' which is 'PRIVATE'.",
            classUnderTest.getDefaultMessage()
        );
    }

    @Test
    void testCreateMessage() {
        assertEquals(OVERRIDDEN_ERROR_MESSAGE, classUnderTest.createMessage(mockValidationErrorMessageCreator));
        verify(mockValidationErrorMessageCreator).createErrorMessage(classUnderTest);
    }

    private CaseFieldEntity caseFieldEntityWithReferenceAndSecurityClassification(
        String caseFieldReference, SecurityClassification securityClassification) {
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setSecurityClassification(securityClassification);
        caseFieldEntity.setReference(caseFieldReference);
        return caseFieldEntity;
    }

    private CaseFieldEntityValidationContext caseFieldEntityValidationContext(
        String parentCaseName, SecurityClassification parentCaseFieldSecurityClassification) {
        CaseFieldEntityValidationContext caseFieldEntityValidationContext =
            mock(CaseFieldEntityValidationContext.class);
        when(caseFieldEntityValidationContext.getCaseName()).thenReturn(parentCaseName);
        when(caseFieldEntityValidationContext.getParentSecurityClassification())
            .thenReturn(parentCaseFieldSecurityClassification);
        return caseFieldEntityValidationContext;
    }

}
