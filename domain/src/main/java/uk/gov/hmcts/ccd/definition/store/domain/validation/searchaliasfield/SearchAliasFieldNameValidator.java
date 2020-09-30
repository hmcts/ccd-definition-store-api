package uk.gov.hmcts.ccd.definition.store.domain.validation.searchaliasfield;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchAliasFieldEntity;

import java.util.regex.Pattern;

@Component
public class SearchAliasFieldNameValidator implements SearchAliasFieldValidator {

    private static final Pattern SEARCH_ALIAS_FIELD_NAME_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9_]{1,}$");

    @Override
    public ValidationResult validate(SearchAliasFieldEntity searchAliasField) {

        ValidationResult validationResult = new ValidationResult();

        if (searchAliasField.getReference() == null
            || !SEARCH_ALIAS_FIELD_NAME_PATTERN.matcher(searchAliasField.getReference()).matches()) {
            validationResult.addError(new ValidationError(
                String.format("Invalid search alias ID '%s' for case type '%s' and case field '%s'. The ID "
                    + "must start with a letter, be exclusively composed of lowercase and/or "
                    + "uppercase letters, numbers and/or underscores, with a minimum length of 2.",
                searchAliasField.getReference(),
                searchAliasField.getCaseType().getReference(),
                searchAliasField.getCaseFieldPath()),
                searchAliasField));
        }

        return validationResult;
    }

}
