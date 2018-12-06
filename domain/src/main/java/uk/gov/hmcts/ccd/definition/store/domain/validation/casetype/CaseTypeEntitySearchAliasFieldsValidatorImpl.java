package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.searchaliasfield.SearchAliasFieldValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

@Component
public class CaseTypeEntitySearchAliasFieldsValidatorImpl implements CaseTypeEntityValidator {

    private final List<SearchAliasFieldValidator> searchAliasFieldValidators;

    @Autowired
    public CaseTypeEntitySearchAliasFieldsValidatorImpl(List<SearchAliasFieldValidator> searchAliasFieldValidators) {
        this.searchAliasFieldValidators = searchAliasFieldValidators;
    }

    @Override
    public ValidationResult validate(CaseTypeEntity caseType) {
        ValidationResult validationResult = new ValidationResult();

        caseType.getSearchAliasFields()
            .forEach(searchAliasField -> searchAliasFieldValidators
                .forEach(validator -> validationResult.merge(validator.validate(searchAliasField))));

        return validationResult;
    }
}
