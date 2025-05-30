package uk.gov.hmcts.ccd.definition.store.domain.validation.searchaliasfield;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchAliasFieldEntity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class SearchAliasFieldNameValidatorTest {

    private final SearchAliasFieldNameValidator validator = new SearchAliasFieldNameValidator();
    private final SearchAliasFieldEntity searchAliasField = new SearchAliasFieldEntity();

    @BeforeEach
    void setUp() {
        searchAliasField.setCaseType(new CaseTypeEntity());
    }

    @Nested
    @DisplayName("Valid search alias field name")
    class ValidAliasFieldName {

        @Test
        @DisplayName("should return no validation errors")
        void shouldReturnNoErrors() {
            searchAliasField.setReference("Abc_123");

            ValidationResult result = validator.validate(searchAliasField);
            assertThat(result.getValidationErrors().size(), is(0));
        }

    }

    @Nested
    @DisplayName("Invalid search alias field name")
    class InvalidAliasFieldName {

        @Test
        @DisplayName("should return validation error for name starting with non-letter")
        void shouldReturnErrorForNameStartingWithNonLetter() {
            searchAliasField.setReference("1Abc");

            ValidationResult result = validator.validate(searchAliasField);
            assertThat(result.getValidationErrors().size(), is(1));
        }

        @Test
        @DisplayName("should return validation error for name containing symbols")
        void shouldReturnErrorForNameWithSymbols() {
            searchAliasField.setReference("Abc*");

            ValidationResult result = validator.validate(searchAliasField);
            assertThat(result.getValidationErrors().size(), is(1));
        }

        @Test
        @DisplayName("should return validation error for name less than 2 characters long")
        void shouldReturnErrorForNameLessThanTwoCharsLong() {
            searchAliasField.setReference("A");

            ValidationResult result = validator.validate(searchAliasField);
            assertThat(result.getValidationErrors().size(), is(1));
        }

    }

}
