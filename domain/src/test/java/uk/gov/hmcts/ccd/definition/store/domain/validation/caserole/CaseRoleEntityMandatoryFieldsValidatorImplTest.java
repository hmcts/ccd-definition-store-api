package uk.gov.hmcts.ccd.definition.store.domain.validation.caserole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("CaseRoleEntity Mandatory Field Validator Tests")
class CaseRoleEntityMandatoryFieldsValidatorImplTest {
    private CaseTypeEntity caseTypeEntity;
    private CaseRoleEntity caseRoleEntity;

    private CaseRoleEntityValidationContext caseRoleEntityValidationContext;
    @InjectMocks
    private CaseRoleEntityMandatoryFieldsValidatorImpl classUnderTest;

    @BeforeEach
    void setUp() {
        caseRoleEntity = new CaseRoleEntity();
        classUnderTest = new CaseRoleEntityMandatoryFieldsValidatorImpl();
        caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setName("Case Type One");
        caseTypeEntity.setReference("Case Type I");
        caseRoleEntityValidationContext = new CaseRoleEntityValidationContext(caseTypeEntity);
    }

    @DisplayName("should return empty validation result in case of no validation failures")
    @Test
    void validCaseRoleEntity() {
        caseRoleEntity.setName("some name");
        caseRoleEntity.setReference("some reference");
        caseRoleEntity.setCaseType(caseTypeEntity);
        final ValidationResult result = classUnderTest.validate(caseRoleEntity, caseRoleEntityValidationContext);
        assertAll(
            () -> assertThat(result.getValidationErrors().size(), is(0)),
            () -> assertThat(result.isValid(), is(true))
        );
    }

    @DisplayName("should fail when caseTypeEntity is null")
    @Test
    void missingCaseType() {
        caseRoleEntity.setName("some name");
        caseRoleEntity.setReference("some reference");
        final ValidationResult result = classUnderTest.validate(caseRoleEntity, caseRoleEntityValidationContext);
        assertAll(
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.isValid(), is(false))
        );
    }

    @DisplayName("should fail when case role name is null")
    @Test
    void missingCaseRoleName() {
        caseRoleEntity.setCaseType(caseTypeEntity);
        caseRoleEntity.setReference("some reference");
        final ValidationResult result = classUnderTest.validate(caseRoleEntity, caseRoleEntityValidationContext);
        assertAll(
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.isValid(), is(false))
        );
    }

    @DisplayName("should fail when case role id is null")
    @Test
    void missingCaseRoleId() {
        caseRoleEntity.setCaseType(caseTypeEntity);
        caseRoleEntity.setName("some name");
        final ValidationResult result = classUnderTest.validate(caseRoleEntity, caseRoleEntityValidationContext);
        assertAll(
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.isValid(), is(false))
        );
    }
}
