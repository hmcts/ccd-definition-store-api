package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ComplexFieldEntityMissingSecurityClassificationValidationErrorTest {

    private static final String OVERRIDDEN_ERROR_MESSAGE = "The overridden error message";

    @Mock
    private ValidationErrorMessageCreator mockValidationErrorMessageCreator;

    private ComplexFieldEntityMissingSecurityClassificationValidationError classUnderTest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockValidationErrorMessageCreator.createErrorMessage(
            any(ComplexFieldEntityMissingSecurityClassificationValidationError.class)))
            .thenReturn(OVERRIDDEN_ERROR_MESSAGE);
        classUnderTest = new ComplexFieldEntityMissingSecurityClassificationValidationError(
            complexFieldEntityWithReference("Complex Field Reference")
        );
    }

    @Test
    public void testDefaultMessage() {
        assertEquals(
            "ComplexField with reference 'Complex Field Reference' must have a Security Classification defined",
            classUnderTest.getDefaultMessage()
        );
    }

    @Test
    public void testCreateMessage() {
        assertEquals(OVERRIDDEN_ERROR_MESSAGE, classUnderTest.createMessage(mockValidationErrorMessageCreator));
        verify(mockValidationErrorMessageCreator).createErrorMessage(classUnderTest);
    }

    private ComplexFieldEntity complexFieldEntityWithReference(String caseFieldReference) {
        ComplexFieldEntity complexFieldEntity = new ComplexFieldEntity();
        complexFieldEntity.setReference(caseFieldReference);
        return complexFieldEntity;
    }

}
