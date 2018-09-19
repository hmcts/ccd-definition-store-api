package uk.gov.hmcts.ccd.definition.store.domain.validation.caserole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class CaseRoleEntityUniquenessValidatorImplTest {
    private final String REFERENCE = "[SOMEID]";
    private CaseTypeEntity caseTypeEntity;
    private CaseRoleEntityValidationContext caseRoleEntityValidationContext;

    @InjectMocks
    private CaseRoleEntityUniquenessValidatorImpl classUnderTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        classUnderTest = new CaseRoleEntityUniquenessValidatorImpl();
        caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setName("Case Type One");
        caseTypeEntity.setReference("Case Type I");
        caseRoleEntityValidationContext = new CaseRoleEntityValidationContext(caseTypeEntity);
        CaseRoleEntity caseRoleEntity = new CaseRoleEntity();
        caseRoleEntity.setReference(REFERENCE);
        caseTypeEntity.addCaseRole(caseRoleEntity);
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
        dupCaseRole.setReference(REFERENCE.toUpperCase());
        caseTypeEntity.addCaseRole(dupCaseRole);

        final ValidationResult result = classUnderTest.validate(dupCaseRole, caseRoleEntityValidationContext);

        assertAll(
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.isValid(), is(false))
        );
    }

    @DisplayName("should fail - reserved Id used")
    @Test
    void reservedCaseRoleId() {
        CaseRoleEntity caseRoleIdWithReservedWord = new CaseRoleEntity();
        caseRoleIdWithReservedWord.setReference("[CREATOR]");
        caseTypeEntity.addCaseRole(caseRoleIdWithReservedWord);

        final ValidationResult result = classUnderTest.validate(caseRoleIdWithReservedWord, caseRoleEntityValidationContext);

        assertAll(
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(), containsString("CaseRole Id " +
                "[CREATOR] is reserved")),
            () -> assertThat(result.isValid(), is(false))
        );
    }
}
