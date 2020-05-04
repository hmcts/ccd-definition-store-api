package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CaseFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationErrorTest {

    private static final String OVERRIDDEN_ERROR_MESSAGE = "The overridden error message";

    @Mock
    private ValidationErrorMessageCreator mockValidationErrorMessageCreator;

    private CaseFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError classUnderTest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockValidationErrorMessageCreator.createErrorMessage(any(CaseFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError.class)))
            .thenReturn(OVERRIDDEN_ERROR_MESSAGE);
        classUnderTest = new CaseFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError(
            caseFieldEntityWithReferenceAndSecurityClassification("Case Field Reference", SecurityClassification.PUBLIC),
            caseFieldEntityValidationContext("Parent Case Name", SecurityClassification.PRIVATE)
        );
    }

    @Test
    public void testDefaultMessage() {
        assertEquals(
            "Security classification for CaseField with reference 'Case Field Reference' "
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

    private CaseFieldEntity caseFieldEntityWithReferenceAndSecurityClassification(String caseFieldReference,
                                                                                  SecurityClassification securityClassification) {
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setSecurityClassification(securityClassification);
        caseFieldEntity.setReference(caseFieldReference);
        return caseFieldEntity;
    }

    private CaseFieldEntityValidationContext caseFieldEntityValidationContext(String parentCaseName,
                                                                              SecurityClassification parentCaseFieldSecurityClassification) {
        CaseFieldEntityValidationContext caseFieldEntityValidationContext = mock(CaseFieldEntityValidationContext.class);
        when(caseFieldEntityValidationContext.getCaseName()).thenReturn(parentCaseName);
        when(caseFieldEntityValidationContext.getParentSecurityClassification()).thenReturn(parentCaseFieldSecurityClassification);
        return caseFieldEntityValidationContext;
    }

}
