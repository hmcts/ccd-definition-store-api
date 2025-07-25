package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;


import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeACLEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeUserRoleEntityBuilder.buildCaseTypeUserRoleEntity;

class CaseTypeEntityInvalidCrudValidationErrorTest {

    private static final String OVERRIDDEN_ERROR_MESSAGE = "The overridden error message";

    @Mock
    private ValidationErrorMessageCreator mockValidationErrorMessageCreator;

    private CaseTypeEntityInvalidCrudValidationError classUnderTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        final CaseTypeACLEntity caseTypeACLEntity = buildCaseTypeUserRoleEntity("NGITB");
        classUnderTest = new CaseTypeEntityInvalidCrudValidationError(caseTypeACLEntity,
            new AuthorisationValidationContext(caseTypeACLEntity.getCaseType()));
        when(mockValidationErrorMessageCreator.createErrorMessage(classUnderTest))
            .thenReturn(OVERRIDDEN_ERROR_MESSAGE);
    }

    @Test
    void testDefaultMessage() {
        assertEquals(
            "Invalid CRUD value 'NGITB' for case type 'case_type'",
            classUnderTest.getDefaultMessage()
        );
    }

    @Test
    void testCreateMessage() {
        assertEquals(OVERRIDDEN_ERROR_MESSAGE, classUnderTest.createMessage(mockValidationErrorMessageCreator));
        verify(mockValidationErrorMessageCreator).createErrorMessage(classUnderTest);
    }
}
