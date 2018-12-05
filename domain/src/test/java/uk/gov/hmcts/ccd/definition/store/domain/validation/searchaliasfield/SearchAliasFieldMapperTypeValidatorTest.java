package uk.gov.hmcts.ccd.definition.store.domain.validation.searchaliasfield;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_NUMBER;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_TEXT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.SearchAliasFieldRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchAliasFieldEntity;

class SearchAliasFieldMapperTypeValidatorTest {

    private static final String SEARCH_ALIAS_REFERENCE = "alias";

    @Mock
    private SearchAliasFieldRepository repository;

    private SearchAliasFieldMapperTypeValidator validator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new SearchAliasFieldMapperTypeValidator(repository);
    }

    @Nested
    @DisplayName("Search alias field matches existing field type")
    class SearchAliasFieldTypesMatch {

        @Test
        @DisplayName("should return no validation error")
        void shouldReturnNoErrors() {
            FieldTypeEntity fieldType = new FieldTypeEntity();
            fieldType.setReference(BASE_TEXT);
            SearchAliasFieldEntity searchAliasField = new SearchAliasFieldEntity();
            searchAliasField.setFieldType(fieldType);
            searchAliasField.setReference(SEARCH_ALIAS_REFERENCE);
            when(repository.findByReference(SEARCH_ALIAS_REFERENCE)).thenReturn(Collections.singletonList(searchAliasField));

            ValidationResult validationResult = validator.validate(searchAliasField);
            assertThat(validationResult.getValidationErrors().size(), is(0));
            verify(repository).findByReference(SEARCH_ALIAS_REFERENCE);
        }

    }

    @Nested
    @DisplayName("Search alias field does not match existing field type")
    class SearchAliasFieldTypeMisMatch {

        @Test
        @DisplayName("should return validation error")
        void shouldReturnValidationErrors() {
            FieldTypeEntity numberType = new FieldTypeEntity();
            numberType.setReference(BASE_NUMBER);
            SearchAliasFieldEntity existingField = new SearchAliasFieldEntity();
            existingField.setFieldType(numberType);
            existingField.setReference(SEARCH_ALIAS_REFERENCE);
            existingField.setCaseType(new CaseTypeEntity());
            when(repository.findByReference(SEARCH_ALIAS_REFERENCE)).thenReturn(Collections.singletonList(existingField));

            FieldTypeEntity textType = new FieldTypeEntity();
            textType.setReference(BASE_TEXT);
            SearchAliasFieldEntity searchAliasField = new SearchAliasFieldEntity();
            searchAliasField.setFieldType(textType);
            searchAliasField.setReference(SEARCH_ALIAS_REFERENCE);

            ValidationResult validationResult = validator.validate(searchAliasField);
            assertThat(validationResult.getValidationErrors().size(), is(1));
            verify(repository).findByReference(SEARCH_ALIAS_REFERENCE);
        }

    }
}
