package uk.gov.hmcts.ccd.definition.store.domain.validation.searchaliasfield;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchAliasFieldEntity;

class SearchAliasFieldUnicityValidatorTest {

    private final SearchAliasFieldUnicityValidator validator = new SearchAliasFieldUnicityValidator();

    @Nested
    @DisplayName("No duplicate search alias fields")
    class NoDuplicates {

        @Test
        @DisplayName("should return no validation errors")
        void shouldReturnNoErrors() {
            SearchAliasFieldEntity searchAliasField = new SearchAliasFieldEntity();
            searchAliasField.setReference("ref");
            CaseTypeEntity caseType = new CaseTypeEntity();
            caseType.setReference("caseType");
            searchAliasField.setCaseType(caseType);

            ValidationResult result = validator.validate(searchAliasField);
            assertThat(result.getValidationErrors().size(), is(0));
        }

    }

    @Nested
    @DisplayName("Duplicate search alias fields")
    class DuplicateFields {

        @Test
        @DisplayName("should return validation errors for duplicate search alias fields for a case type")
        void shouldReturnErrors() {
            String aliasName = "alias";
            CaseTypeEntity caseType = new CaseTypeEntity();
            caseType.setReference("caseType");
            SearchAliasFieldEntity searchAliasField1 = new SearchAliasFieldEntity();
            searchAliasField1.setReference(aliasName);
            searchAliasField1.setCaseType(caseType);

            ValidationResult result = validator.validate(searchAliasField1);
            assertThat(result.getValidationErrors().size(), is(0));

            SearchAliasFieldEntity searchAliasField2 = new SearchAliasFieldEntity();
            searchAliasField2.setReference(aliasName);
            searchAliasField2.setCaseType(caseType);

            ValidationResult result1 = validator.validate(searchAliasField2);
            assertThat(result1.getValidationErrors().size(), is(1));
            assertThat(result1.getValidationErrors().get(0).getDefaultMessage(), startsWith("Duplicate search alias ID"));
        }

    }
}
