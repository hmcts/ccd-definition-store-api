package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.caserole.CaseRoleEntityFieldValueValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.caserole.CaseRoleEntityMandatoryFieldsValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.caserole.CaseRoleEntityUniquenessValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@DisplayName("Case Role Validator Tests")
class CaseTypeEntityCaseRoleValidatorImplTest {
    @Mock
    CaseRoleEntityMandatoryFieldsValidatorImpl mandatoryValidator;

    @Mock
    CaseRoleEntityFieldValueValidatorImpl fieldValueValidator;

    @Mock
    CaseRoleEntityUniquenessValidatorImpl uniquenessValidator;

    CaseTypeEntityCaseRoleValidatorImpl classUnderTest;
    private CaseTypeEntity caseType;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        classUnderTest = new CaseTypeEntityCaseRoleValidatorImpl(Arrays.asList(mandatoryValidator,
            fieldValueValidator, uniquenessValidator));
        caseType = new CaseTypeEntity();
        caseType.setReference("Case Type I");
    }

    @DisplayName("Should return empty validation result in case of no validation failures")
    @Test
    void shouldReturnEmptyValidationResultWhenNoErrors() {
        when(mandatoryValidator.validate(any(), any())).thenReturn(new ValidationResult());
        when(fieldValueValidator.validate(any(), any())).thenReturn(new ValidationResult());
        when(uniquenessValidator.validate(any(), any())).thenReturn(new ValidationResult());

        final ValidationResult result = classUnderTest.validate(caseType);

        assertAll(
            () -> assertThat(result.getValidationErrors().size(), is(0)),
            () -> assertThat(result.isValid(), is(true))
        );
    }


    @DisplayName("Should return Validation Error in case of validation failures")
    @Test
    void shouldReturnValidationResultWithValidationErrorWhenThereAreErrors() {
        caseType.addCaseRole(new CaseRoleEntity());

        ValidationResult vr1 = new ValidationResult();
        vr1.addError(new CaseRoleEntityMandatoryFieldsValidatorImpl.ValidationError(
            "Mandatory field validation error message...", new CaseRoleEntity()));
        when(mandatoryValidator.validate(any(), any())).thenReturn(vr1);

        ValidationResult vr2 = new ValidationResult();
        vr2.addError(new CaseRoleEntityFieldValueValidatorImpl.ValidationError(
            "Field value validation error message...", new CaseRoleEntity()));
        when(fieldValueValidator.validate(any(), any())).thenReturn(vr2);

        ValidationResult vr3 = new ValidationResult();
        vr3.addError(new CaseRoleEntityUniquenessValidatorImpl.ValidationError(
            "Unique field value validation error message...", new CaseRoleEntity()));
        when(uniquenessValidator.validate(any(), any())).thenReturn(vr3);

        final ValidationResult result = classUnderTest.validate(caseType);

        assertAll(
            () -> assertThat(result.getValidationErrors().size(), is(3)),
            () -> assertThat(result.isValid(), is(false))
        );
    }
}
