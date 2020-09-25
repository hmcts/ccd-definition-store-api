package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.searchaliasfield.SearchAliasFieldValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchAliasFieldEntity;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CaseTypeEntitySearchAliasFieldsValidatorImplTest {

    @Mock
    private SearchAliasFieldValidator searchAliasFieldValidator;

    private CaseTypeEntitySearchAliasFieldsValidatorImpl caseTypeEntitySearchAliasFieldsValidator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        caseTypeEntitySearchAliasFieldsValidator = new CaseTypeEntitySearchAliasFieldsValidatorImpl(
            Collections.singletonList(searchAliasFieldValidator));
    }

    @Nested
    @DisplayName("Valid search alias fields")
    class ValidSearchAliasFields {

        @Test
        @DisplayName("should return no validation errors")
        void shouldReturnNoErrors() {
            SearchAliasFieldEntity searchAliasField = new SearchAliasFieldEntity();
            CaseTypeEntity caseType = new CaseTypeEntity();
            caseType.addSearchAliasFields(Collections.singletonList(searchAliasField));
            when(searchAliasFieldValidator.validate(any(SearchAliasFieldEntity.class)))
                .thenReturn(new ValidationResult());

            ValidationResult validationResult = caseTypeEntitySearchAliasFieldsValidator.validate(caseType);
            assertThat(validationResult.getValidationErrors().size(), is(0));
            verify(searchAliasFieldValidator).validate(searchAliasField);
        }

    }

    @Nested
    @DisplayName("Invalid search alias field")
    class InvalidSearchAliasField {

        @Test
        @DisplayName("should return validation errors")
        void shouldReturnValidationErrors() {
            SearchAliasFieldEntity searchAliasField = new SearchAliasFieldEntity();
            CaseTypeEntity caseType = new CaseTypeEntity();
            caseType.addSearchAliasFields(Collections.singletonList(searchAliasField));
            ValidationResult validationResult = new ValidationResult();
            SearchAliasFieldValidator.ValidationError validationError = new SearchAliasFieldValidator
                .ValidationError("Error", searchAliasField);
            validationResult.addError(validationError);
            when(searchAliasFieldValidator.validate(any(SearchAliasFieldEntity.class))).thenReturn(validationResult);

            ValidationResult result = caseTypeEntitySearchAliasFieldsValidator.validate(caseType);
            assertThat(result.getValidationErrors().size(), is(1));
            assertThat(result.getValidationErrors().get(0), is(validationError));
            verify(searchAliasFieldValidator).validate(searchAliasField);
        }

    }
}
