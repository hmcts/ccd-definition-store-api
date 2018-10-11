package uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.*;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("Generic Layout Entity Validator Implementation Tests")
class GenericLayoutEntityValidatorImplTest {
    private static final String CASE_FIELD = "Case Field I";
    private GenericLayoutEntityValidatorImpl validator;
    private GenericLayoutEntity entity;
    private CaseTypeEntity caseType;
    private CaseFieldEntity caseField;

    @BeforeEach
    void setUp() {
        validator = new GenericLayoutEntityValidatorImpl();

        caseType = new CaseTypeEntity();
        caseType.setReference("Case Type I");

        caseField = new CaseFieldEntity();
        caseField.setReference(CASE_FIELD);
    }

    @Nested
    @DisplayName("SearchInputCaseFieldEntity validation tests")
    class SearchInputCaseFieldEntityTests {
        @BeforeEach
        void setUp() {
            entity = new SearchInputCaseFieldEntity();
            entity.setLabel("Label");
        }

        @Test
        void shouldValidateGoodEntity() {
            entity.setCaseField(caseField);
            entity.setCaseType(caseType);
            final ValidationResult result = validator.validate(entity);

            assertAll(
                () -> assertThat(entity, instanceOf(SearchInputCaseFieldEntity.class)),
                () -> assertThat(result.isValid(), is(true))
            );
        }

        @Test
        void shouldFailWhenDisplayOrderIsNotPositive() {
            entity.setCaseField(caseField);
            entity.setCaseType(caseType);
            entity.setOrder(-1);
            final ValidationResult result = validator.validate(entity);

            assertAll(
                () -> assertThat(entity, instanceOf(SearchInputCaseFieldEntity.class)),
                () -> assertThat(result.isValid(), is(false)),
                () -> assertThat(result.getValidationErrors().size(), is(1)),
                () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                         is("DisplayOrder '-1' needs to be a valid integer for row with label 'Label', case field '" +
                                CASE_FIELD + "'"))
            );
        }

        @Test
        void shouldFailWhenCaseTypeIsEmpty() {
            entity.setCaseField(caseField);
            final ValidationResult result = validator.validate(entity);

            assertAll(
                () -> assertThat(entity, instanceOf(SearchInputCaseFieldEntity.class)),
                () -> assertThat(result.isValid(), is(false)),
                () -> assertThat(result.getValidationErrors().size(), is(1)),
                () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                    is("Case Type cannot be empty for row with label 'Label', case field 'Case Field I'"))
            );
        }

        @Test
        void shouldFailWhenCaseFieldIsEmpty() {
            entity.setCaseType(caseType);
            final ValidationResult result = validator.validate(entity);

            assertAll(
                () -> assertThat(entity, instanceOf(SearchInputCaseFieldEntity.class)),
                () -> assertThat(result.isValid(), is(false)),
                () -> assertThat(result.getValidationErrors().size(), is(1)),
                () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                    is("Case Field cannot be empty for row with label 'Label', case type 'Case Type I'"))
            );
        }

        @Test
        void shouldFailWhenBothCaseTypeAndCaseFieldAreEmpty() {
            final ValidationResult result = validator.validate(entity);

            assertAll(
                () -> assertThat(entity, instanceOf(SearchInputCaseFieldEntity.class)),
                () -> assertThat(result.isValid(), is(false)),
                () -> assertThat(result.getValidationErrors().size(), is(2)),
                () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                    is("Case Type cannot be empty for row with label 'Label', case field ''")),
                () ->
                    assertThat(result.getValidationErrors().get(1).getDefaultMessage(),
                        is("Case Field cannot be empty for row with label 'Label', case type ''"))
            );
        }
    }

    @Nested
    @DisplayName("SearchResultCaseFieldEntity validation tests")
    class SearchResultCaseFieldEntityTests {
        @BeforeEach
        void setUp() {
            entity = new SearchResultCaseFieldEntity();
            entity.setLabel("Label");
        }

        @Test
        void shouldValidateGoodEntity() {
            entity.setCaseField(caseField);
            entity.setCaseType(caseType);
            final ValidationResult result = validator.validate(entity);

            assertAll(
                () -> assertTrue(entity instanceof SearchResultCaseFieldEntity),
                () -> assertThat(result.isValid(), is(true))
            );
        }

        @Test
        void shouldFailWhenDisplayOrderIsNotPositive() {
            entity.setCaseField(caseField);
            entity.setCaseType(caseType);
            entity.setOrder(-1);
            final ValidationResult result = validator.validate(entity);

            assertAll(
                () -> assertThat(entity, instanceOf(SearchResultCaseFieldEntity.class)),
                () -> assertThat(result.isValid(), is(false)),
                () -> assertThat(result.getValidationErrors().size(), is(1)),
                () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                         is("DisplayOrder '-1' needs to be a valid integer for row with label 'Label', case field '" +
                                CASE_FIELD + "'"))
            );
        }

        @Test
        void shouldFailWhenCaseTypeIsEmpty() {
            entity.setCaseField(caseField);
            final ValidationResult result = validator.validate(entity);

            assertAll(
                () -> assertThat(entity, instanceOf(SearchResultCaseFieldEntity.class)),
                () -> assertThat(result.isValid(), is(false)),
                () -> assertThat(result.getValidationErrors().size(), is(1)),
                () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                    is("Case Type cannot be empty for row with label 'Label', case field 'Case Field I'"))
            );
        }

        @Test
        void shouldFailWhenCaseFieldIsEmpty() {
            entity.setCaseType(caseType);
            final ValidationResult result = validator.validate(entity);

            assertAll(
                () -> assertThat(entity, instanceOf(SearchResultCaseFieldEntity.class)),
                () -> assertThat(result.isValid(), is(false)),
                () -> assertThat(result.getValidationErrors().size(), is(1)),
                () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                    is("Case Field cannot be empty for row with label 'Label', case type 'Case Type I'"))
            );
        }

        @Test
        void shouldFailWhenBothCaseTypeAndCaseFieldAreEmpty() {
            final ValidationResult result = validator.validate(entity);

            assertAll(
                () -> assertThat(entity, instanceOf(SearchResultCaseFieldEntity.class)),
                () -> assertThat(result.isValid(), is(false)),
                () -> assertThat(result.getValidationErrors().size(), is(2)),
                () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                    is("Case Type cannot be empty for row with label 'Label', case field ''")),
                () ->
                    assertThat(result.getValidationErrors().get(1).getDefaultMessage(),
                        is("Case Field cannot be empty for row with label 'Label', case type ''"))
            );
        }
    }

    @Nested
    @DisplayName("WorkBasketInputCaseFieldEntity validation tests")
    class WorkBasketInputCaseFieldEntityTests {
        @BeforeEach
        void setUp() {
            entity = new WorkBasketInputCaseFieldEntity();
            entity.setLabel("Label");
        }

        @Test
        void shouldValidateGoodEntity() {
            entity.setCaseField(caseField);
            entity.setCaseType(caseType);
            final ValidationResult result = validator.validate(entity);

            assertAll(
                () -> assertThat(entity, instanceOf(WorkBasketInputCaseFieldEntity.class)),
                () -> assertThat(result.isValid(), is(true))
            );
        }

        @Test
        void shouldFailWhenDisplayOrderIsNotPositive() {
            entity.setCaseField(caseField);
            entity.setCaseType(caseType);
            entity.setOrder(-1);
            final ValidationResult result = validator.validate(entity);

            assertAll(
                () -> assertThat(entity, instanceOf(WorkBasketInputCaseFieldEntity.class)),
                () -> assertThat(result.isValid(), is(false)),
                () -> assertThat(result.getValidationErrors().size(), is(1)),
                () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                    is("DisplayOrder '-1' needs to be a valid integer for row with label 'Label', case field '" +
                       CASE_FIELD + "'"))
            );
        }

        @Test
        void shouldFailWhenCaseTypeIsEmpty() {
            entity.setCaseField(caseField);
            final ValidationResult result = validator.validate(entity);

            assertAll(
                () -> assertThat(entity, instanceOf(WorkBasketInputCaseFieldEntity.class)),
                () -> assertThat(result.isValid(), is(false)),
                () -> assertThat(result.getValidationErrors().size(), is(1)),
                () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                    is("Case Type cannot be empty for row with label 'Label', case field 'Case Field I'"))
            );
        }

        @Test
        void shouldFailWhenCaseFieldIsEmpty() {
            entity.setCaseType(caseType);
            final ValidationResult result = validator.validate(entity);

            assertAll(
                () -> assertThat(entity, instanceOf(WorkBasketInputCaseFieldEntity.class)),
                () -> assertThat(result.isValid(), is(false)),
                () -> assertThat(result.getValidationErrors().size(), is(1)),
                () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                    is("Case Field cannot be empty for row with label 'Label', case type 'Case Type I'"))
            );
        }

        @Test
        void shouldFailWhenBothCaseTypeAndCaseFieldAreEmpty() {
            final ValidationResult result = validator.validate(entity);

            assertAll(
                () -> assertThat(entity, instanceOf(WorkBasketInputCaseFieldEntity.class)),
                () -> assertThat(result.isValid(), is(false)),
                () -> assertThat(result.getValidationErrors().size(), is(2)),
                () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                    is("Case Type cannot be empty for row with label 'Label', case field ''")),
                () ->
                    assertThat(result.getValidationErrors().get(1).getDefaultMessage(),
                        is("Case Field cannot be empty for row with label 'Label', case type ''"))
            );
        }
    }

    @Nested
    @DisplayName("WorkBasketCaseFieldEntity validation tests")
    class WorkBasketCaseFieldEntityTests {
        @BeforeEach
        void setUp() {
            entity = new WorkBasketCaseFieldEntity();
            entity.setLabel("Label");
        }

        @Test
        void shouldValidateGoodEntity() {
            entity.setCaseField(caseField);
            entity.setCaseType(caseType);
            final ValidationResult result = validator.validate(entity);

            assertAll(
                () -> assertThat(entity, instanceOf(WorkBasketCaseFieldEntity.class)),
                () -> assertThat(result.isValid(), is(true))
            );
        }

        @Test
        void shouldFailWhenDisplayOrderIsNotPositive() {
            entity.setCaseField(caseField);
            entity.setCaseType(caseType);
            entity.setOrder(-1);
            final ValidationResult result = validator.validate(entity);

            assertAll(
                () -> assertThat(entity, instanceOf(WorkBasketCaseFieldEntity.class)),
                () -> assertThat(result.isValid(), is(false)),
                () -> assertThat(result.getValidationErrors().size(), is(1)),
                () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                    is("DisplayOrder '-1' needs to be a valid integer for row with label 'Label', case field '" +
                           CASE_FIELD + "'"))
            );
        }

        @Test
        void shouldFailWhenCaseTypeIsEmpty() {
            entity.setCaseField(caseField);
            final ValidationResult result = validator.validate(entity);

            assertAll(
                () -> assertThat(entity, instanceOf(WorkBasketCaseFieldEntity.class)),
                () -> assertThat(result.isValid(), is(false)),
                () -> assertThat(result.getValidationErrors().size(), is(1)),
                () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                    is("Case Type cannot be empty for row with label 'Label', case field 'Case Field I'"))
            );
        }

        @Test
        void shouldFailWhenCaseFieldIsEmpty() {
            entity.setCaseType(caseType);
            final ValidationResult result = validator.validate(entity);

            assertAll(
                () -> assertThat(entity, instanceOf(WorkBasketCaseFieldEntity.class)),
                () -> assertThat(result.isValid(), is(false)),
                () -> assertThat(result.getValidationErrors().size(), is(1)),
                () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                    is("Case Field cannot be empty for row with label 'Label', case type 'Case Type I'"))
            );
        }

        @Test
        void shouldFailWhenBothCaseTypeAndCaseFieldAreEmpty() {
            final ValidationResult result = validator.validate(entity);

            assertAll(
                () -> assertThat(entity, instanceOf(WorkBasketCaseFieldEntity.class)),
                () -> assertThat(result.isValid(), is(false)),
                () -> assertThat(result.getValidationErrors().size(), is(2)),
                () -> assertThat(result.getValidationErrors().get(0).getDefaultMessage(),
                    is("Case Type cannot be empty for row with label 'Label', case field ''")),
                () ->
                    assertThat(result.getValidationErrors().get(1).getDefaultMessage(),
                        is("Case Field cannot be empty for row with label 'Label', case type ''"))
            );
        }
    }
}
