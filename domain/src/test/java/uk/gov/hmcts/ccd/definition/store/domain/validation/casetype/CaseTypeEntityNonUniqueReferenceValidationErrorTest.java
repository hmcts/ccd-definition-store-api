package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CaseTypeEntityNonUniqueReferenceValidationErrorTest {

    private static final String OVERRIDDEN_ERROR_MESSAGE = "The overridden error message";

    @Mock
    private ValidationErrorMessageCreator mockValidationErrorMessageCreator;

    private CaseTypeEntityNonUniqueReferenceValidationError classUnderTest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockValidationErrorMessageCreator.createErrorMessage(
            any(CaseTypeEntityNonUniqueReferenceValidationError.class)))
            .thenReturn(OVERRIDDEN_ERROR_MESSAGE);
        classUnderTest = new CaseTypeEntityNonUniqueReferenceValidationError(
            caseTypeEntityWithReference("Charley says Dont talk to strangers"));
    }

    @Test
    public void testDefaultMessage() {
        assertEquals("Case Type with reference 'Charley says Dont talk to strangers' already exists. "
                + "Case types must be unique across all existing jurisdictions.",
            classUnderTest.getDefaultMessage());
    }

    @Test
    public void testCreateMessage() {
        assertEquals(OVERRIDDEN_ERROR_MESSAGE, classUnderTest.createMessage(mockValidationErrorMessageCreator));
        verify(mockValidationErrorMessageCreator).createErrorMessage(classUnderTest);
    }

    private CaseTypeEntity caseTypeEntityWithReference(String name) {
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference(name);
        return caseTypeEntity;
    }

}
