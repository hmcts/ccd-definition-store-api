package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.TestValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityValidator;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.Arrays;
import java.util.Collection;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public class CaseTypeEntityCaseFieldsValidatorImplTest {

    private static final String CASE_TYPE_NAME = "Case Type Name";

    private static final SecurityClassification CASE_TYPE_SECURITY_CLASSIFICATION = SecurityClassification.PRIVATE;

    @Mock
    private CaseFieldEntityValidator caseFieldEntityValidator1;

    @Mock
    private CaseFieldEntityValidator caseFieldEntityValidator2;

    @Captor
    private ArgumentCaptor<Collection<CaseFieldEntity>> captor;

    private CaseFieldEntity caseFieldEntity1 = new CaseFieldEntity();

    private CaseFieldEntity caseFieldEntity2 = new CaseFieldEntity();

    private CaseFieldEntity caseFieldEntity3 = new CaseFieldEntity();

    private Collection<CaseFieldEntity> caseFieldEntities = Arrays.asList(
        caseFieldEntity1, caseFieldEntity2, caseFieldEntity3);

    private CaseTypeEntityCaseFieldsValidatorImpl classUnderTest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(caseFieldEntityValidator1.validate(any(), any())).thenReturn(new ValidationResult());
        when(caseFieldEntityValidator2.validate(any(), any())).thenReturn(new ValidationResult());

        classUnderTest = new CaseTypeEntityCaseFieldsValidatorImpl(
            Arrays.asList(caseFieldEntityValidator1, caseFieldEntityValidator2)
        );
    }

    @Test
    public void caseFieldsAllValid_allValidatorsCalledWithContextBuiltFromCaseType_EmptyValidationResultReturned() {


        ValidationResult validationResult = classUnderTest.validate(caseType(
            CASE_TYPE_NAME, CASE_TYPE_SECURITY_CLASSIFICATION, caseFieldEntities));

        assertTrue(validationResult.isValid());
        assertTrue(validationResult.getValidationErrors().isEmpty());

        verifyAllValidatorsCalledOnceForEachCaseFieldWithCorrectValidationContext();

    }

    @SuppressWarnings("checkstyle:LineLength")
    @Test
    public void caseFields1And3AreInvalid_allValidatorsCalledWithContextBuiltFromCaseType_ValidationResultWithErrorsForCaseFieldEntity1And3Returned() {

        when(caseFieldEntityValidator1.validate(eq(caseFieldEntity1), any()))
            .thenReturn(validationResultWithError(validationErrorWithDefaultMessage(
                "caseFieldEntityValidator1 failed for caseFieldEntity1")));
        when(caseFieldEntityValidator2.validate(eq(caseFieldEntity3), any()))
            .thenReturn(validationResultWithError(validationErrorWithDefaultMessage(
                "caseFieldEntityValidator2 failed for caseFieldEntity3")));

        ValidationResult validationResult = classUnderTest.validate(caseType(
            CASE_TYPE_NAME, CASE_TYPE_SECURITY_CLASSIFICATION, caseFieldEntities));

        assertFalse(validationResult.isValid());
        assertEquals(2, validationResult.getValidationErrors().size());

        assertThat(validationResult.getValidationErrors(), allOf(
            hasItem(matchesValidationErrorWithDefaultMessage("caseFieldEntityValidator1 failed for caseFieldEntity1")),
            hasItem(matchesValidationErrorWithDefaultMessage("caseFieldEntityValidator2 failed for caseFieldEntity3"))
            )
        );

        verifyAllValidatorsCalledOnceForEachCaseFieldWithCorrectValidationContext();

    }

    private CaseTypeEntity caseType(String caseTypeName,
                                    SecurityClassification securityClassification,
                                    Collection<CaseFieldEntity> caseFields) {
        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setName(caseTypeName);
        caseType.setSecurityClassification(securityClassification);
        caseType.addCaseFields(caseFields);
        return caseType;
    }

    private void verifyAllValidatorsCalledOnceForEachCaseFieldWithCorrectValidationContext() {
        verify(caseFieldEntityValidator1).validate(
            eq(caseFieldEntity1),
            argThat(matchesCaseFieldEntityValidationContext(CASE_TYPE_NAME, CASE_TYPE_SECURITY_CLASSIFICATION))
        );
        verify(caseFieldEntityValidator1).validate(
            eq(caseFieldEntity2),
            argThat(matchesCaseFieldEntityValidationContext(CASE_TYPE_NAME, CASE_TYPE_SECURITY_CLASSIFICATION))
        );
        verify(caseFieldEntityValidator1).validate(
            eq(caseFieldEntity3),
            argThat(matchesCaseFieldEntityValidationContext(CASE_TYPE_NAME, CASE_TYPE_SECURITY_CLASSIFICATION))
        );

        verify(caseFieldEntityValidator2).validate(
            eq(caseFieldEntity1),
            argThat(matchesCaseFieldEntityValidationContext(CASE_TYPE_NAME, CASE_TYPE_SECURITY_CLASSIFICATION))
        );
        verify(caseFieldEntityValidator2).validate(
            eq(caseFieldEntity2),
            argThat(matchesCaseFieldEntityValidationContext(CASE_TYPE_NAME, CASE_TYPE_SECURITY_CLASSIFICATION))
        );
        verify(caseFieldEntityValidator2).validate(
            eq(caseFieldEntity3),
            argThat(matchesCaseFieldEntityValidationContext(CASE_TYPE_NAME, CASE_TYPE_SECURITY_CLASSIFICATION))
        );
    }

    private <T> Matcher<T> matchesCaseFieldEntityValidationContext(String caseTypeName,
                                                                   SecurityClassification securityClassification) {
        return new BaseMatcher<T>() {
            @Override
            public boolean matches(Object o) {
                return o instanceof CaseFieldEntityValidationContext
                    && ((CaseFieldEntityValidationContext) o).getCaseName() == caseTypeName
                    && ((CaseFieldEntityValidationContext) o)
                    .getParentSecurityClassification() == securityClassification;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(
                    " a CaseFieldEntityValidationContext containing the reference to the Case Type Name and "
                        + "SecurityClassification of the CaseType");
            }
        };
    }

    private <T> Matcher<T> matchesValidationErrorWithDefaultMessage(String defaultMessage) {
        return new BaseMatcher<T>() {
            @Override
            public boolean matches(Object o) {
                return o instanceof ValidationError
                    && ((ValidationError) o).getDefaultMessage().equals(defaultMessage);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a ValidationError with defaultMessage " + defaultMessage);
            }
        };
    }

    private ValidationResult validationResultWithError(ValidationError validationError) {
        ValidationResult validationResult = new ValidationResult();
        validationResult.addError(validationError);
        return validationResult;
    }

    private ValidationError validationErrorWithDefaultMessage(String defaultMessage) {
        return new TestValidationError(defaultMessage);
    }

}
