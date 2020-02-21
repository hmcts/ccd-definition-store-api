package uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.datetime.DateTimeFormatParser;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;

import java.util.regex.Pattern;

@Component
public class DateTimeEntryValidatorImpl implements DisplayContextParameterValidator {

    private final static Pattern ALLOWED_CHARACTERS_PATTERN = Pattern.compile("[yMdHmsS]+");

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
    public void validate(final String parameterValue) throws Exception {
        dateTimeFormatParser.parseDateTimeFormat(parameterValue, ALLOWED_CHARACTERS_PATTERN);
    }
}
