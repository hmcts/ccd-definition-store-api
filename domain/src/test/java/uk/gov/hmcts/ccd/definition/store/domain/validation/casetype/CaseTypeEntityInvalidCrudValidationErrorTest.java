package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeACLEntity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeUserRoleEntityBuilder.buildCaseTypeUserRoleEntity;

public class CaseTypeEntityInvalidCrudValidationErrorTest {

    private static final String OVERRIDDEN_ERROR_MESSAGE = "The overridden error message";

    @Mock
    private ValidationErrorMessageCreator mockValidationErrorMessageCreator;

    private CaseTypeEntityInvalidCrudValidationError classUnderTest;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        final CaseTypeACLEntity caseTypeACLEntity = buildCaseTypeUserRoleEntity("NGITB");
        classUnderTest = new CaseTypeEntityInvalidCrudValidationError(caseTypeACLEntity,
            new AuthorisationValidationContext(caseTypeACLEntity.getCaseType()));
        when(mockValidationErrorMessageCreator.createErrorMessage(classUnderTest))
            .thenReturn(OVERRIDDEN_ERROR_MESSAGE);
    }

    @Test
    public void testDefaultMessage() {
        assertEquals(
            "Invalid CRUD value 'NGITB' for case type 'case_type'",
            classUnderTest.getDefaultMessage()
        );
    }

    @Test
    public void testCreateMessage() {
        assertEquals(OVERRIDDEN_ERROR_MESSAGE, classUnderTest.createMessage(mockValidationErrorMessageCreator));
        verify(mockValidationErrorMessageCreator).createErrorMessage(classUnderTest);
    }
}
