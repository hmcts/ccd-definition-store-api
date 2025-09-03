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

class CaseTypeEntityInvalidAccessProfileValidationErrorTest {

    private static final String OVERRIDDEN_ERROR_MESSAGE = "The overridden error message";

    @Mock
    private ValidationErrorMessageCreator mockValidationErrorMessageCreator;

    private CaseTypeEntityInvalidAccessProfileValidationError classUnderTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        final CaseTypeACLEntity entity = buildCaseTypeUserRoleEntity("1yLLoMwpA7");
        classUnderTest = new CaseTypeEntityInvalidAccessProfileValidationError(entity,
            new AuthorisationValidationContext(entity.getCaseType()));
        when(mockValidationErrorMessageCreator.createErrorMessage(classUnderTest))
            .thenReturn(OVERRIDDEN_ERROR_MESSAGE);
    }

    @Test
    void testDefaultMessage() {
        assertEquals(
            "Invalid AccessProfile is not defined for case type 'case_type'",
            classUnderTest.getDefaultMessage()
        );
    }

    @Test
    void testCreateMessage() {
        assertEquals(OVERRIDDEN_ERROR_MESSAGE, classUnderTest.createMessage(mockValidationErrorMessageCreator));
        verify(mockValidationErrorMessageCreator).createErrorMessage(classUnderTest);
    }
}
