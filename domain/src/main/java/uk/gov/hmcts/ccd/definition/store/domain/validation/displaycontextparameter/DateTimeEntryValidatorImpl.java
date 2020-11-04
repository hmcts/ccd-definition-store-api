package uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.datetime.DateTimeFormatParser;
import uk.gov.hmcts.ccd.definition.store.domain.datetime.InvalidDateTimeFormatException;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;

import java.util.regex.Pattern;

import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_DATE;

@Component
public class DateTimeEntryValidatorImpl implements DisplayContextParameterValidator {

    private static final Pattern NOT_ALLOWED_CHARACTERS_PATTERN_DATETIME = Pattern.compile("[YDVzOXxZ]");
    private static final Pattern NOT_ALLOWED_CHARACTERS_PATTERN_DATE = Pattern.compile("[YDahKkHmsSAnNVzOXxZ]");

    private DateTimeFormatParser dateTimeFormatParser;

    @Autowired
    public DateTimeEntryValidatorImpl(DateTimeFormatParser dateTimeFormatParser) {
        this.dateTimeFormatParser = dateTimeFormatParser;
    }

    @Override
    public DisplayContextParameterType getType() {
        return DisplayContextParameterType.DATETIMEENTRY;
    }

    @Override
    public void validate(final String parameterValue, String fieldType) throws InvalidDateTimeFormatException {
        Pattern pattern = ((fieldType.equals(BASE_DATE))
            ? NOT_ALLOWED_CHARACTERS_PATTERN_DATE : NOT_ALLOWED_CHARACTERS_PATTERN_DATETIME);
        dateTimeFormatParser.parseDateTimeFormat(parameterValue, pattern);
    }
}
