package uk.gov.hmcts.ccd.definition.store.domain.validation.searchaliasfield;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchAliasFieldEntity;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.defaultString;

@Component
@RequestScope
public class SearchAliasFieldUnicityValidator implements SearchAliasFieldValidator {

    private final Map<String, String> searchFieldAliasCaseTypeMap = new HashMap<>();

    @Override
    public ValidationResult validate(SearchAliasFieldEntity searchAliasField) {

        ValidationResult validationResult = new ValidationResult();

        if (searchAliasField.getCaseType().getReference().equalsIgnoreCase(
            searchFieldAliasCaseTypeMap.get(searchAliasField.getReference()))) {
            validationResult.addError(
                new ValidationError(String.format("Duplicate search alias ID '%s' for case type '%s'. "
                    + "Search Alias ID must be unique for a case type",
                defaultString(searchAliasField.getReference()),
                searchAliasField.getCaseType().getReference()),
                searchAliasField));
        } else {
            searchFieldAliasCaseTypeMap.put(
                searchAliasField.getReference(), searchAliasField.getCaseType().getReference());
        }

        return validationResult;
    }

}
