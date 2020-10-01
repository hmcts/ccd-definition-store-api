package uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.TestValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield.ComplexFieldValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.Arrays;
import java.util.List;

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

public class FieldTypeComplexFieldsValidatorImplTest {

    @Mock
    private ComplexFieldValidator complexFieldEntityValidator1;

    @Mock
    private ComplexFieldValidator complexFieldEntityValidator2;

    private ComplexFieldEntity complexFieldEntity1 = new ComplexFieldEntity();

    private ComplexFieldEntity complexFieldEntity2 = new ComplexFieldEntity();

    private ComplexFieldEntity complexFieldEntity3 = new ComplexFieldEntity();

    private List<ComplexFieldEntity> complexFieldEntities = Arrays.asList(
        complexFieldEntity1, complexFieldEntity2, complexFieldEntity3);

    private FieldTypeComplexFieldsValidatorImpl classUnderTest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(complexFieldEntityValidator1.validate(any(), any())).thenReturn(new ValidationResult());
        when(complexFieldEntityValidator2.validate(any(), any())).thenReturn(new ValidationResult());

        classUnderTest = new FieldTypeComplexFieldsValidatorImpl(
            Arrays.asList(complexFieldEntityValidator1, complexFieldEntityValidator2)
        );
    }

    @Test
    public void caseFieldsAllValid_allValidatorsCalledWithContextBuiltFromCaseType_EmptyValidationResultReturned() {


        ValidationResult validationResult = classUnderTest.validate(null, fieldType(complexFieldEntities));

        assertTrue(validationResult.isValid());
        assertTrue(validationResult.getValidationErrors().isEmpty());

        verifyAllValidatorsCalledOnceForEachComplexField();

    }

    @SuppressWarnings("checkstyle:LineLength")
    @Test
    public void caseFields1And3AreInvalid_allValidatorsCalledWithContextBuiltFromCaseType_ValidationResultWithErrorsForCaseFieldEntity1And3Returned() {

        when(complexFieldEntityValidator1.validate(eq(complexFieldEntity1), any()))
            .thenReturn(validationResultWithError(validationErrorWithDefaultMessage(
                "caseFieldEntityValidator1 failed for complexFieldEntity1")));
        when(complexFieldEntityValidator2.validate(eq(complexFieldEntity3), any()))
            .thenReturn(validationResultWithError(validationErrorWithDefaultMessage(
                "complexFieldEntityValidator2 failed for complexFieldEntity3")));

        ValidationResult validationResult = classUnderTest.validate(null, fieldType(complexFieldEntities));

        assertFalse(validationResult.isValid());
        assertEquals(2, validationResult.getValidationErrors().size());

        assertThat(validationResult.getValidationErrors(), allOf(
            hasItem(matchesValidationErrorWithDefaultMessage(
                "caseFieldEntityValidator1 failed for complexFieldEntity1")),
            hasItem(matchesValidationErrorWithDefaultMessage(
                "complexFieldEntityValidator2 failed for complexFieldEntity3"))
            )
        );

        verifyAllValidatorsCalledOnceForEachComplexField();

    }

    private FieldTypeEntity fieldType(List<ComplexFieldEntity> complexFields) {
        FieldTypeEntity fieldType = new FieldTypeEntity();
        fieldType.addComplexFields(complexFields);
        return fieldType;
    }

    private void verifyAllValidatorsCalledOnceForEachComplexField() {
        verify(complexFieldEntityValidator1).validate(
            eq(complexFieldEntity1), any()
        );
        verify(complexFieldEntityValidator1).validate(
            eq(complexFieldEntity2), any()
        );
        verify(complexFieldEntityValidator1).validate(
            eq(complexFieldEntity3), any()
        );

        verify(complexFieldEntityValidator2).validate(
            eq(complexFieldEntity1), any()
        );
        verify(complexFieldEntityValidator2).validate(
            eq(complexFieldEntity2), any()
        );
        verify(complexFieldEntityValidator2).validate(
            eq(complexFieldEntity3), any()
        );
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
