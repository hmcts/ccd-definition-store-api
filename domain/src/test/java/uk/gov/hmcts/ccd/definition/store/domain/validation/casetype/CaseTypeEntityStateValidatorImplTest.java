package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;


import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.state.StateEntityACLValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.state.StateEntityCrudValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("CaseType Entity Validator Implementation Tests")
class CaseTypeEntityStateValidatorImplTest {
    private CaseTypeEntity caseType;

    @Mock
    private StateEntityCrudValidatorImpl crudValidatorImpl;

    @Mock
    private StateEntityACLValidatorImpl userRoleValidatorImpl;

    @InjectMocks
    private CaseTypeEntityStateValidatorImpl classUnderTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        classUnderTest = new CaseTypeEntityStateValidatorImpl(Arrays.asList(crudValidatorImpl, userRoleValidatorImpl));
        caseType = new CaseTypeEntity();
        caseType.setReference("Case Type I");
    }

    @DisplayName("Should return empty validation result in case of no validation failures")
    @Test
    void shouldReturnEmptyValidationResultWhenNoErrors() {
        when(crudValidatorImpl.validate(any(), any())).thenReturn(new ValidationResult());
        when(userRoleValidatorImpl.validate(any(), any())).thenReturn(new ValidationResult());

        final ValidationResult result = classUnderTest.validate(caseType);

        assertAll(
            () -> assertThat(result.getValidationErrors().size(), is(0)),
            () -> assertThat(result.isValid(), is(true))
        );
    }

    @DisplayName("Should return Validation Error in case of validation failures")
    @Test
    void shouldReturnValidationResultWithValidationErrorWhenThereAreErrors() {
        ValidationResult vr1 = new ValidationResult();
        vr1.addError(new StateEntityACLValidatorImpl.ValidationError(
            "Default user role validation error message...", new StateACLEntity()));
        when(crudValidatorImpl.validate(any(), any())).thenReturn(vr1);
        ValidationResult vr2 = new ValidationResult();
        vr1.addError(new StateEntityCrudValidatorImpl.ValidationError(
            "Default crud validation error message...", new StateACLEntity()));
        caseType.addState(new StateEntity());
        when(userRoleValidatorImpl.validate(any(), any())).thenReturn(vr2);

        final ValidationResult result = classUnderTest.validate(caseType);

        assertAll(
            () -> assertThat(result.getValidationErrors().size(), is(2)),
            () -> assertThat(result.isValid(), is(false))
        );
    }

}


