package uk.gov.hmcts.ccd.definition.store.domain.validation.caserole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CaseRoleEntity Field Value Validator Tests")
class CaseRoleEntityFieldValueValidatorImplTest {
    private CaseRoleEntity caseRoleEntity;
    private CaseRoleEntityValidationContext caseRoleEntityValidationContext;

    @InjectMocks
    private CaseRoleEntityFieldValueValidatorImpl classUnderTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        classUnderTest = new CaseRoleEntityFieldValueValidatorImpl();
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setName("Case Type One");
        caseTypeEntity.setReference("Case Type I");
        caseRoleEntityValidationContext = new CaseRoleEntityValidationContext(caseTypeEntity);
        caseRoleEntity = new CaseRoleEntity();
    }

    @Nested
    @DisplayName("CaseRole ID Tests")
    class CaseRoleIdTests {

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
            caseRoleEntity.setReference("[ABCDEFGHIJKLMNOPRSQTXVWYZABCDEFGHIJKLMNOPRSQTXVWYZ]");
            final ValidationResult result = classUnderTest.validate(caseRoleEntity, caseRoleEntityValidationContext);
            assertAll(
                () -> assertThat(result.getValidationErrors().size(), is(1)),
                () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(), containsString("CaseRole ID" +
                    " must be less than")),
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
                () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(), containsString("CaseRole ID" +
                    " must be only characters with no space and between '[]'")),
                () -> assertThat(result.isValid(), is(false))
            );
        }
    }

    @Nested
    @DisplayName("CaseRole Name Tests")
    class CaseRoleNameTests {
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

        @DisplayName("should fail - CaseRole name too long")
        @Test
        void veryLongCaseRoleName() {
            caseRoleEntity.setName("ABCDEFGHIJKLMNOPRSQTXVWYZABCDEFGHIJKLMNOPRSQTXVWYZ");
            final ValidationResult result = classUnderTest.validate(caseRoleEntity, caseRoleEntityValidationContext);
            assertAll(
                () -> assertThat(result.getValidationErrors().size(), is(1)),
                () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(), containsString("CaseRole " +
                    "name " +
                    "must be less than")),
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
                () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(), containsString("CaseRole " +
                    "name must be non-empty characters for case type")),
                () -> assertThat(result.isValid(), is(false))
            );
        }
    }
}
