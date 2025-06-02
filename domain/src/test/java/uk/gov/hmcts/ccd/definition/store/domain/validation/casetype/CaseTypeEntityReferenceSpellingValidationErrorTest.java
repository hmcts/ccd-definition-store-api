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

class CaseTypeEntityReferenceSpellingValidationErrorTest {

    private static final String OVERRIDDEN_ERROR_MESSAGE = "The overridden error message";

    @Mock
    private ValidationErrorMessageCreator mockValidationErrorMessageCreator;

    private CaseTypeEntityReferenceSpellingValidationError classUnderTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockValidationErrorMessageCreator.createErrorMessage(any(
            CaseTypeEntityReferenceSpellingValidationError.class)))
            .thenReturn(OVERRIDDEN_ERROR_MESSAGE);
        classUnderTest = new CaseTypeEntityReferenceSpellingValidationError(
            "Definitive spelling", caseTypeEntityWithReference("Imported spelling"));
    }

    @Test
    void testDefaultMessage() {
        assertEquals("Current spelling of this Case Type ID is 'Definitive spelling' but the imported Case Type "
            + "ID was 'Imported spelling'.", classUnderTest.getDefaultMessage());
    }

    @Test
    void testCreateMessage() {
        assertEquals(OVERRIDDEN_ERROR_MESSAGE, classUnderTest.createMessage(mockValidationErrorMessageCreator));
        verify(mockValidationErrorMessageCreator).createErrorMessage(classUnderTest);
    }

    private CaseTypeEntity caseTypeEntityWithReference(String name) {
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference(name);
        return caseTypeEntity;
    }
}
