package uk.gov.hmcts.ccd.definition.store.domain.validation.caserole;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

class CaseRoleEntityUniquenessValidatorImplTest {
    private final String reference = "[SOMEID]";
    private CaseTypeEntity caseTypeEntity;
    private CaseRoleEntityValidationContext caseRoleEntityValidationContext;

    @InjectMocks
    private CaseRoleEntityUniquenessValidatorImpl classUnderTest;

    @BeforeEach
    void setUp() {
        classUnderTest = new CaseRoleEntityUniquenessValidatorImpl();
        caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setName("Case Type One");
        caseTypeEntity.setReference("Case Type I");
        CaseRoleEntity caseRoleEntity = new CaseRoleEntity();
        caseTypeEntity.addCaseRole(caseRoleEntity);
        caseRoleEntityValidationContext = new CaseRoleEntityValidationContext(caseTypeEntity);
        caseRoleEntity.setReference(reference);
    }

    @DisplayName("should pass - when no validation failures")
    @Test
    void validCaseRoleId() {
        CaseRoleEntity otherCaseRoleEntity = new CaseRoleEntity();
        otherCaseRoleEntity.setReference("[SOMEOTHERID]");
        caseTypeEntity.addCaseRole(otherCaseRoleEntity);

        final ValidationResult result = classUnderTest.validate(otherCaseRoleEntity, caseRoleEntityValidationContext);

        assertAll(
            () -> assertThat(result.getValidationErrors().size(), is(0)),
            () -> assertThat(result.isValid(), is(true))
        );
    }

    @DisplayName("should fail - when duplicate case role reference")
    @Test
    void duplicateCaseRole() {
        CaseRoleEntity dupCaseRole = new CaseRoleEntity();
        dupCaseRole.setReference(reference.toUpperCase());
        caseTypeEntity.addCaseRole(dupCaseRole);

        final ValidationResult result = classUnderTest.validate(dupCaseRole, caseRoleEntityValidationContext);

        assertAll(
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.isValid(), is(false))
        );
    }
}
