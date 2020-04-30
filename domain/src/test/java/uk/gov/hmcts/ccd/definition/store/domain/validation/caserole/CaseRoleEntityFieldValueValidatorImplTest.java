package uk.gov.hmcts.ccd.definition.store.domain.validation.caserole;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

@DisplayName("CaseRoleEntity Field Value Validator Tests")
class CaseRoleEntityFieldValueValidatorImplTest {
    private CaseRoleEntity caseRoleEntity;
    private CaseRoleEntityValidationContext caseRoleEntityValidationContext;

    @InjectMocks
    private CaseRoleEntityFieldValueValidatorImpl classUnderTest;

    @BeforeEach
    void setUp() {
        caseRoleEntity = new CaseRoleEntity();
        caseRoleEntity.setName("some name");
        caseRoleEntity.setReference("[SOMEREF]");
        classUnderTest = new CaseRoleEntityFieldValueValidatorImpl();
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setName("Case Type One");
        caseTypeEntity.setReference("Case Type I");
        caseTypeEntity.addCaseRole(caseRoleEntity);
        caseRoleEntityValidationContext = new CaseRoleEntityValidationContext(caseTypeEntity);
    }

    @DisplayName("should pass - when no validation failures")
    @Test
    void validCaseRoleId() {
        caseRoleEntity.setReference("[someid]");
        final ValidationResult result = classUnderTest.validate(caseRoleEntity, caseRoleEntityValidationContext);
        assertAll(
            () -> assertThat(result.getValidationErrors().size(), is(0)),
            () -> assertThat(result.isValid(), is(true))
        );
    }

    @DisplayName("should pass - CaseRole ID with at least a character")
    @Test
    void caseRoleIdWithOnlyOneCharacter() {
        caseRoleEntity.setReference("[a]");
        final ValidationResult result = classUnderTest.validate(caseRoleEntity, caseRoleEntityValidationContext);
        assertAll(
            () -> assertThat(result.getValidationErrors().size(), is(0)),
            () -> assertThat(result.isValid(), is(true))
        );
    }

    @DisplayName("should fail - CaseRole ID is null")
    @Test
    void nullCaseRoleId() {
        caseRoleEntity.setReference(null);
        final ValidationResult result = classUnderTest.validate(caseRoleEntity, caseRoleEntityValidationContext);
        assertAll(
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.isValid(), is(false))
        );
    }

    @DisplayName("should fail - CaseRole ID without square bracket")
    @Test
    void invalidCaseRoleId() {
        caseRoleEntity.setReference("someid");
        final ValidationResult result = classUnderTest.validate(caseRoleEntity, caseRoleEntityValidationContext);
        assertAll(
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.isValid(), is(false))
        );
    }

    @DisplayName("should fail - CaseRole ID with empty square brackets")
    @Test
    void emptyCaseRoleId() {
        caseRoleEntity.setReference("[]");
        final ValidationResult result = classUnderTest.validate(caseRoleEntity, caseRoleEntityValidationContext);
        assertAll(
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.isValid(), is(false))
        );
    }

    @DisplayName("should fail - CaseRole ID with single space in square brackets")
    @Test
    void caseRoleIdAsASingleSpace() {
        caseRoleEntity.setReference("[ ]");
        final ValidationResult result = classUnderTest.validate(caseRoleEntity, caseRoleEntityValidationContext);
        assertAll(
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.isValid(), is(false))
        );
    }

    @DisplayName("should fail - CaseRole ID valid but too long")
    @Test
    void veryLongValidCaseRoleId() {
        caseRoleEntity.setReference("[ABCDEFGHIJKLMNOPRSQTXVWYZABCDEFGHIJKLMNOPRSQTXVWYZABCDEFGHIJKLMNOPRSQTXVWYZ"
            + "ABCDEFGHIJKLMNOPRSQTXVWYZABCDEFGHIJKLMNOPRSQTXVWYZABCDEFGHIJKLMNOPRSQTXVWYZ"
            + "ABCDEFGHIJKLMNOPRSQTXVWYZABCDEFGHIJKLMNOPRSQTXVWYZABCDEFGHIJKLMNOPRSQTXVWYZ"
            + "ABCDEFGHIJKLMNOPRSQTXVWYZABCDEFGHIJKLMNOPRSQTXVWYZABCDEFGHIJKLMNOPRSQTXVWYZ]");
        final ValidationResult result = classUnderTest.validate(caseRoleEntity, caseRoleEntityValidationContext);
        assertAll(
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(), containsString("CaseRole ID"
                + " must be less than")),
            () -> assertThat(result.isValid(), is(false))
        );
    }

    @DisplayName("should fail - CaseRole ID invalid and too long")
    @Test
    void veryLongInvalidCaseRoleId() {
        caseRoleEntity.setReference("[ABCDEFGHIJKLMNOPRSQTXVWYZABCDEFGHIJKLMNOPRSQTXVWYZ");
        final ValidationResult result = classUnderTest.validate(caseRoleEntity, caseRoleEntityValidationContext);
        assertAll(
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(), containsString("CaseRole ID"
                + " must be only characters with no space and between '[]'")),
            () -> assertThat(result.isValid(), is(false))
        );
    }

    @DisplayName("should pass - CaseRole name fair")
    @Test
    void validCaseRoleName() {
        caseRoleEntity.setName("ABCDEFGHIJ");
        final ValidationResult result = classUnderTest.validate(caseRoleEntity, caseRoleEntityValidationContext);
        assertAll(
            () -> assertThat(result.getValidationErrors().size(), is(0)),
            () -> assertThat(result.isValid(), is(true))
        );
    }

    @DisplayName("should fail - CaseRole name is null")
    @Test
    void nullCaseRoleName() {
        caseRoleEntity.setName(null);
        final ValidationResult result = classUnderTest.validate(caseRoleEntity, caseRoleEntityValidationContext);
        assertAll(
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.isValid(), is(false))
        );
    }

    @DisplayName("should fail - CaseRole name too long")
    @Test
    void veryLongCaseRoleName() {
        caseRoleEntity.setName("ABCDEFGHIJKLMNOPRSQTXVWYZABCDEFGHIJKLMNOPRSQTXVWYZ"
            + "ABCDEFGHIJKLMNOPRSQTXVWYZABCDEFGHIJKLMNOPRSQTXVWYZ"
            + "ABCDEFGHIJKLMNOPRSQTXVWYZABCDEFGHIJKLMNOPRSQTXVWYZ"
            + "ABCDEFGHIJKLMNOPRSQTXVWYZABCDEFGHIJKLMNOPRSQTXVWYZ"
            + "ABCDEFGHIJKLMNOPRSQTXVWYZABCDEFGHIJKLMNOPRSQTXVWYZ"
            + "ABCDEFGHIJKLMNOPRSQTXVWYZABCDEFGHIJKLMNOPRSQTXVWYZ"
            + "ABCDEFGHIJKLMNOPRSQTXVWYZABCDEFGHIJKLMNOPRSQTXVWYZ");
        final ValidationResult result = classUnderTest.validate(caseRoleEntity, caseRoleEntityValidationContext);
        assertAll(
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(), containsString("CaseRole "
                + "name must be less than")),
            () -> assertThat(result.isValid(), is(false))
        );
    }

    @DisplayName("should fail - CaseRole name only space")
    @Test
    void caseRoleNameOnlySpace() {
        caseRoleEntity.setName("    ");
        final ValidationResult result = classUnderTest.validate(caseRoleEntity, caseRoleEntityValidationContext);
        assertAll(
            () -> assertThat(result.getValidationErrors().size(), is(1)),
            () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(), containsString("CaseRole "
                + "name must be non-empty characters for case type")),
            () -> assertThat(result.isValid(), is(false))
        );
    }
}
