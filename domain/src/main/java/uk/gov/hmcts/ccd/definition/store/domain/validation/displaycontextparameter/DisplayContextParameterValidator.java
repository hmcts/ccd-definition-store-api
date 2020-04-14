package uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter;

import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;

import java.util.regex.*;

public interface DisplayContextParameterValidator {

    Pattern NOT_ALLOWED_CHARACTERS_PATTERN_DATETIME = Pattern.compile("[VzOXxZ]");
    Pattern NOT_ALLOWED_CHARACTERS_PATTERN_DATE = Pattern.compile("[ahKkHmsSAnNVzOXxZ]");

    DisplayContextParameterType getType();

    void validate(String parameterValue, String fieldType) throws Exception;
}
