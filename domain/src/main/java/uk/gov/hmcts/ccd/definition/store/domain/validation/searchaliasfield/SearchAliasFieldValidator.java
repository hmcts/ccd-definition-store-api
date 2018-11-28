package uk.gov.hmcts.ccd.definition.store.domain.validation.searchaliasfield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchAliasFieldEntity;

public interface SearchAliasFieldValidator {

    ValidationResult validate(SearchAliasFieldEntity searchAliasFieldEntity, SearchAliasFieldValidationContext context);

    class ValidationError extends SimpleValidationError<SearchAliasFieldEntity> {

        private static final long serialVersionUID = 549847081729577435L;

        public ValidationError(String defaultMessage, SearchAliasFieldEntity entity) {
            super(defaultMessage, entity);
        }
    }
}
