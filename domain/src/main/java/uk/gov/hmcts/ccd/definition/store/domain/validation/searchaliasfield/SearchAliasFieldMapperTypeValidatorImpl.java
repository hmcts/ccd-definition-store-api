package uk.gov.hmcts.ccd.definition.store.domain.validation.searchaliasfield;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.SearchAliasFieldRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchAliasFieldEntity;

@Component
public class SearchAliasFieldMapperTypeValidatorImpl implements SearchAliasFieldValidator {

    private final SearchAliasFieldRepository repository;

    @Autowired
    public SearchAliasFieldMapperTypeValidatorImpl(SearchAliasFieldRepository repository) {
        this.repository = repository;
    }

    @Override
    public ValidationResult validate(SearchAliasFieldEntity caseField, SearchAliasFieldValidationContext context) {

        ValidationResult validationResult = new ValidationResult();

        // TODO - validate


        return validationResult;
    }
}
