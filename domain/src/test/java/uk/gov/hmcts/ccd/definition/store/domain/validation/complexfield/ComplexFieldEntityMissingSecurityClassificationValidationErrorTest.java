package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ComplexFieldEntityMissingSecurityClassificationValidationErrorTest {

    private static final String OVERRIDDEN_ERROR_MESSAGE = "The overridden error message";

    @Mock
    private ValidationErrorMessageCreator mockValidationErrorMessageCreator;

    private ComplexFieldEntityMissingSecurityClassificationValidationError classUnderTest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
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
