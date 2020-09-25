package uk.gov.hmcts.ccd.definition.store.domain.validation.searchaliasfield;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchAliasFieldEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;
import uk.gov.hmcts.ccd.definition.store.utils.SearchAliasFieldBuilder;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class SearchAliasFieldUnicityValidatorTest {

    private final SearchAliasFieldUnicityValidator validator = new SearchAliasFieldUnicityValidator();
    private final CaseTypeEntity caseType = new CaseTypeBuilder().withReference("caseType").build();

    @Nested
    @DisplayName("No duplicate search alias fields")
    class NoDuplicates {

        @Test
        @DisplayName("should return no validation errors")
        void shouldReturnNoErrors() {
            SearchAliasFieldEntity searchAliasField = new SearchAliasFieldBuilder("ref")
                .withCaseType(caseType).build();

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
            SearchAliasFieldEntity searchAliasField1 = new SearchAliasFieldBuilder(aliasName)
                .withCaseType(caseType).build();

            ValidationResult result = validator.validate(searchAliasField1);
            assertThat(result.getValidationErrors().size(), is(0));

            SearchAliasFieldEntity searchAliasField2 = new SearchAliasFieldBuilder(aliasName)
                .withCaseType(caseType).build();

            ValidationResult result1 = validator.validate(searchAliasField2);
            assertThat(result1.getValidationErrors().size(), is(1));
            assertThat(result1.getValidationErrors().get(0).getDefaultMessage(),
                startsWith("Duplicate search alias ID"));
        }

    }
}
