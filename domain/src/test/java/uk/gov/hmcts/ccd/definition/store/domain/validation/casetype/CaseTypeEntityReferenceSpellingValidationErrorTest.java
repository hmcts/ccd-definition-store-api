package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CaseTypeEntityReferenceSpellingValidationErrorTest {

    private static final String OVERRIDDEN_ERROR_MESSAGE = "The overridden error message";

    @Mock
    private ValidationErrorMessageCreator mockValidationErrorMessageCreator;

    private CaseTypeEntityReferenceSpellingValidationError classUnderTest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockValidationErrorMessageCreator.createErrorMessage(any(
            CaseTypeEntityReferenceSpellingValidationError.class)))
            .thenReturn(OVERRIDDEN_ERROR_MESSAGE);
        classUnderTest = new CaseTypeEntityReferenceSpellingValidationError(
            "Definitive spelling", caseTypeEntityWithReference("Imported spelling"));
    }

    @Test
    public void testDefaultMessage() {
        assertEquals("Current spelling of this Case Type ID is 'Definitive spelling' but the imported Case Type "
            + "ID was 'Imported spelling'.", classUnderTest.getDefaultMessage());
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
