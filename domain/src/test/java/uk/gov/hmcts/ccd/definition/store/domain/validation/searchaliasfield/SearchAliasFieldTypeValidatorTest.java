package uk.gov.hmcts.ccd.definition.store.domain.validation.searchaliasfield;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.SearchAliasFieldRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchAliasFieldEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;
import uk.gov.hmcts.ccd.definition.store.utils.SearchAliasFieldBuilder;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_NUMBER;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_TEXT;

class SearchAliasFieldTypeValidatorTest {

    private static final String SEARCH_ALIAS_REFERENCE = "alias";

    private final CaseTypeEntity caseType = new CaseTypeBuilder().withReference("caseType").build();

    @Mock
    private SearchAliasFieldRepository repository;

    private SearchAliasFieldTypeValidator validator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new SearchAliasFieldTypeValidator(repository);
    }

    @Nested
    @DisplayName("Search alias field type is set")
    class FieldTypeSet {

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
    @DisplayName("Search alias field type is not set")
    class FieldTypeNotSet {

        @Test
        @DisplayName("should return validation errors for null field type")
        void shouldReturnErrors() {
            String aliasName = "alias";
            SearchAliasFieldEntity searchAliasField = new SearchAliasFieldBuilder(aliasName)
                .withCaseType(caseType).build();
            searchAliasField.setFieldType(null);

            ValidationResult result1 = validator.validate(searchAliasField);
            assertThat(result1.getValidationErrors().size(), is(1));
            assertThat(result1.getValidationErrors().get(0).getDefaultMessage(), startsWith("Invalid case field"));
        }

    }

    @Nested
    @DisplayName("Search alias field matches existing field type for same alias name")
    class SearchAliasFieldTypesMatch {

        @Test
        @DisplayName("should return no validation error")
        void shouldReturnNoErrors() {
            SearchAliasFieldEntity searchAliasField = new SearchAliasFieldBuilder(SEARCH_ALIAS_REFERENCE)
                .withFieldType(BASE_TEXT).build();
            when(repository.findByReference(SEARCH_ALIAS_REFERENCE))
                .thenReturn(Collections.singletonList(searchAliasField));

            ValidationResult validationResult = validator.validate(searchAliasField);
            assertThat(validationResult.getValidationErrors().size(), is(0));
            verify(repository).findByReference(SEARCH_ALIAS_REFERENCE);
        }

    }

    @Nested
    @DisplayName("Search alias field does not match existing field type for same alias name")
    class SearchAliasFieldTypeMisMatch {

        @Test
        @DisplayName("should return validation error for existing alias with different field type")
        void shouldReturnValidationErrorsExistingAlias() {
            SearchAliasFieldEntity existingField = new SearchAliasFieldBuilder(SEARCH_ALIAS_REFERENCE)
                .withFieldType(BASE_NUMBER)
                .withCaseType(new CaseTypeEntity())
                .build();
            when(repository.findByReference(SEARCH_ALIAS_REFERENCE))
                .thenReturn(Collections.singletonList(existingField));

            SearchAliasFieldEntity searchAliasField = new SearchAliasFieldBuilder(SEARCH_ALIAS_REFERENCE)
                .withFieldType(BASE_TEXT)
                .withCaseType(new CaseTypeEntity())
                .build();

            ValidationResult validationResult = validator.validate(searchAliasField);
            assertThat(validationResult.getValidationErrors().size(), is(1));
            verify(repository).findByReference(SEARCH_ALIAS_REFERENCE);
        }

        @Test
        @DisplayName("should return validation error for new alias with different field type")
        void shouldReturnValidationErrorsNewAlias() {
            SearchAliasFieldEntity searchAliasField1 = new SearchAliasFieldBuilder(SEARCH_ALIAS_REFERENCE)
                .withFieldType(BASE_TEXT)
                .withCaseType(new CaseTypeEntity())
                .build();

            SearchAliasFieldEntity searchAliasField2 = new SearchAliasFieldBuilder(SEARCH_ALIAS_REFERENCE)
                .withFieldType(BASE_NUMBER)
                .withCaseType(new CaseTypeEntity())
                .build();

            validator.validate(searchAliasField1);
            ValidationResult validationResult1 = validator.validate(searchAliasField2);
            assertThat(validationResult1.getValidationErrors().size(), is(1));
        }

    }
}
