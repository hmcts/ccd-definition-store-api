package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ComplexFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationErrorTest {

    private static final String OVERRIDDEN_ERROR_MESSAGE = "The overridden error message";

    @Mock
    private ValidationErrorMessageCreator mockValidationErrorMessageCreator;

    @Mock
    private CaseFieldComplexFieldEntityValidator.ValidationContext mockComplexFieldEntityValidationContext;

    private ComplexFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError classUnderTest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
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
    public void testDefaultMessage() {
        assertEquals(
            "Security classification for ComplexField with reference 'Complex Field Reference' "
                + "has a less restrictive security classification of 'PUBLIC' than its parent CaseField "
                + "'Parent CaseField Reference' which is 'PRIVATE'.",
            classUnderTest.getDefaultMessage()
        );
    }

    @Test
    public void testCreateMessage() {
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
