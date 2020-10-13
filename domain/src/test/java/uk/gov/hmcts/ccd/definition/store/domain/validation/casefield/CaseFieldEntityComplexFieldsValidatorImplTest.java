package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.service.FieldTypeService;
import uk.gov.hmcts.ccd.definition.store.domain.validation.TestValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield.CaseFieldComplexFieldEntityValidator;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.Arrays;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public class CaseFieldEntityComplexFieldsValidatorImplTest {

    private static final String CASE_TYPE_NAME = "Case Type Name";

    private static final String CASE_FIELD_REFERENCE = "Case Field Reference";

    private static final SecurityClassification CASE_FIELD_PARENT_SECURITY_CLASSIFICATION =
        SecurityClassification.PUBLIC;

    private static final SecurityClassification CASE_FIELD_SECURITY_CLASSIFICATION = SecurityClassification.PRIVATE;

    @Mock
    private CaseFieldComplexFieldEntityValidator complexFieldEntityValidator1;

    @Mock
    private CaseFieldComplexFieldEntityValidator complexFieldEntityValidator2;

    @Mock
    private FieldTypeService fieldTypeService;

    private ComplexFieldEntity complexFieldEntity1 = new ComplexFieldEntity();

    private ComplexFieldEntity complexFieldEntity2 = new ComplexFieldEntity();

    private ComplexFieldEntity complexFieldEntity3 = new ComplexFieldEntity();

    private FieldTypeEntity predefinedComplexType1 = new FieldTypeEntity();

    private FieldTypeEntity predefinedComplexType2 = new FieldTypeEntity();

    private FieldTypeEntity predefinedComplexType3 = new FieldTypeEntity();

    private CaseFieldEntityComplexFieldsValidatorImpl classUnderTest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(complexFieldEntityValidator1.validate(any(), any())).thenReturn(new ValidationResult());
        when(complexFieldEntityValidator2.validate(any(), any())).thenReturn(new ValidationResult());

        when(fieldTypeService.getPredefinedComplexTypes()).thenReturn(Arrays.asList(
            predefinedComplexType1, predefinedComplexType2, predefinedComplexType3
            )
        );

        classUnderTest = new CaseFieldEntityComplexFieldsValidatorImpl(
            Arrays.asList(complexFieldEntityValidator1, complexFieldEntityValidator2),
            fieldTypeService
        );
    }

    @SuppressWarnings("checkstyle:LineLength")
    @Test
    public void complexFieldsAllValid_allValidatorsCalledWithContextBuiltFromCaseFieldEntityAndCaseFieldEntityValidationContext_EmptyValidationResultReturned() {

        ValidationResult validationResult = classUnderTest.validate(
            caseFieldEntity(),
            caseFieldEntityValidationContext(CASE_TYPE_NAME, CASE_FIELD_PARENT_SECURITY_CLASSIFICATION)
        );

        assertTrue(validationResult.isValid());
        assertTrue(validationResult.getValidationErrors().isEmpty());

        verifyAllValidatorsCalledOnceForEachComplexFieldWithCorrectValidationContext();

    }

    @SuppressWarnings("checkstyle:LineLength")
    @Test
    public void complexFields1And3AreInvalid_allValidatorsCalledWithContextBuiltFromCaseFieldEntityAndCaseFieldEntityValidationContext_ValidationResultWithErrorsForComplexFieldEntity1And3Returned() {

        when(complexFieldEntityValidator1.validate(eq(complexFieldEntity1), any()))
            .thenReturn(validationResultWithError(validationErrorWithDefaultMessage(
                "complexFieldEntityValidator1 failed for complexFieldEntity1")));
        when(complexFieldEntityValidator2.validate(eq(complexFieldEntity3), any()))
            .thenReturn(validationResultWithError(validationErrorWithDefaultMessage(
                "complexFieldEntityValidator2 failed for complexFieldEntity3")));

        ValidationResult validationResult = classUnderTest.validate(
            caseFieldEntity(),
            caseFieldEntityValidationContext(CASE_TYPE_NAME, CASE_FIELD_PARENT_SECURITY_CLASSIFICATION)
        );

        assertFalse(validationResult.isValid());
        assertEquals(2, validationResult.getValidationErrors().size());

        assertThat(validationResult.getValidationErrors(), allOf(
            hasItem(matchesValidationErrorWithDefaultMessage(
                "complexFieldEntityValidator1 failed for complexFieldEntity1")),
            hasItem(matchesValidationErrorWithDefaultMessage(
                "complexFieldEntityValidator2 failed for complexFieldEntity3"))
            )
        );

        verifyAllValidatorsCalledOnceForEachComplexFieldWithCorrectValidationContext();

    }

    private CaseFieldEntityValidationContext caseFieldEntityValidationContext(
        String parentCaseName, SecurityClassification parentCaseFieldSecurityClassification) {
        CaseFieldEntityValidationContext caseFieldEntityValidationContext =
            mock(CaseFieldEntityValidationContext.class);
        when(caseFieldEntityValidationContext.getCaseName()).thenReturn(parentCaseName);
        when(caseFieldEntityValidationContext.getParentSecurityClassification())
            .thenReturn(parentCaseFieldSecurityClassification);
        return caseFieldEntityValidationContext;
    }

    private void verifyAllValidatorsCalledOnceForEachComplexFieldWithCorrectValidationContext() {
        verify(complexFieldEntityValidator1).validate(
            eq(complexFieldEntity1),
            argThat(matchesComplexFieldEntityValidationContext(
                CASE_TYPE_NAME, CASE_FIELD_REFERENCE, CASE_FIELD_SECURITY_CLASSIFICATION))
        );
        verify(complexFieldEntityValidator1).validate(
            eq(complexFieldEntity2),
            argThat(matchesComplexFieldEntityValidationContext(
                CASE_TYPE_NAME, CASE_FIELD_REFERENCE, CASE_FIELD_SECURITY_CLASSIFICATION))
        );
        verify(complexFieldEntityValidator1).validate(
            eq(complexFieldEntity3),
            argThat(matchesComplexFieldEntityValidationContext(
                CASE_TYPE_NAME, CASE_FIELD_REFERENCE, CASE_FIELD_SECURITY_CLASSIFICATION))
        );

        verify(complexFieldEntityValidator2).validate(
            eq(complexFieldEntity1),
            argThat(matchesComplexFieldEntityValidationContext(
                CASE_TYPE_NAME, CASE_FIELD_REFERENCE, CASE_FIELD_SECURITY_CLASSIFICATION))
        );
        verify(complexFieldEntityValidator2).validate(
            eq(complexFieldEntity2),
            argThat(matchesComplexFieldEntityValidationContext(
                CASE_TYPE_NAME, CASE_FIELD_REFERENCE, CASE_FIELD_SECURITY_CLASSIFICATION))
        );
        verify(complexFieldEntityValidator2).validate(
            eq(complexFieldEntity3),
            argThat(matchesComplexFieldEntityValidationContext(
                CASE_TYPE_NAME, CASE_FIELD_REFERENCE, CASE_FIELD_SECURITY_CLASSIFICATION))
        );
    }

    private CaseFieldEntity caseFieldEntity() {
        FieldTypeEntity fieldType = new FieldTypeEntity();
        fieldType.addComplexFields(Arrays.asList(complexFieldEntity1, complexFieldEntity2, complexFieldEntity3));

        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setSecurityClassification(CASE_FIELD_SECURITY_CLASSIFICATION);
        caseFieldEntity.setReference(CASE_FIELD_REFERENCE);
        caseFieldEntity.setFieldType(fieldType);
        return caseFieldEntity;
    }

    private <T> Matcher<T> matchesComplexFieldEntityValidationContext(String caseTypeName,
                                                                      String caseFieldReference,
                                                                      SecurityClassification securityClassification) {
        return new BaseMatcher<T>() {
            @Override
            public boolean matches(Object o) {
                return o instanceof CaseFieldComplexFieldEntityValidator.ValidationContext
                    && ((CaseFieldComplexFieldEntityValidator.ValidationContext) o).getCaseName() == caseTypeName
                    && ((CaseFieldComplexFieldEntityValidator.ValidationContext) o)
                    .getCaseFieldReference() == caseFieldReference
                    && ((CaseFieldComplexFieldEntityValidator.ValidationContext) o)
                    .getParentSecurityClassification() == securityClassification
                    && ((CaseFieldComplexFieldEntityValidator.ValidationContext) o)
                    .getPreDefinedComplexTypes().size() == 3
                    && ((CaseFieldComplexFieldEntityValidator.ValidationContext) o)
                    .getPreDefinedComplexTypes().contains(predefinedComplexType1)
                    && ((CaseFieldComplexFieldEntityValidator.ValidationContext) o)
                    .getPreDefinedComplexTypes().contains(predefinedComplexType2)
                    && ((CaseFieldComplexFieldEntityValidator.ValidationContext) o)
                    .getPreDefinedComplexTypes().contains(predefinedComplexType3);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(
                    " a ComplexFieldEntityValidationContext containing the reference to the Case Type Name, "
                        + "Case Field reference and SecurityClassification of the CaseField");
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
