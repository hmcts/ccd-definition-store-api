package uk.gov.hmcts.ccd.definition.store.domain.validation.searchaliasfield;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.defaultString;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchAliasFieldEntity;

@Component
public class SearchAliasFieldNameValidatorImpl implements SearchAliasFieldValidator {

    private static final Pattern SEARCH_ALIAS_FIELD_NAME_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9_]{1,}$");

    @Override
    public ValidationResult validate(SearchAliasFieldEntity searchAliasField, SearchAliasFieldValidationContext context) {

        ValidationResult validationResult = new ValidationResult();

        Matcher matcher = SEARCH_ALIAS_FIELD_NAME_PATTERN.matcher(searchAliasField.getReference());
        if (!matcher.matches()) {
            validationResult.addError(new ValidationError(String.format("Invalid search alias field name '%s' for case type '%s', case field '%s'. The name "
                                                                            + "should start with a letter, be exclusively composed of lowercase and/or "
                                                                            + "uppercase letters, numbers and underscores, with a minimum length of 2.",
                                                                        defaultString(searchAliasField.getReference()),
                                                                        context.getCaseType(),
                                                                        context.getCaseField()), searchAliasField));
        }

        return validationResult;
    }

}
