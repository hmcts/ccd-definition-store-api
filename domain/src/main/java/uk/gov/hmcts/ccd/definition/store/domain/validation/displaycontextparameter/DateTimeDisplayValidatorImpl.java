package uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.datetime.DateTimeFormatParser;
import uk.gov.hmcts.ccd.definition.store.domain.datetime.InvalidDateTimeFormatException;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;

@Component
public class DateTimeDisplayValidatorImpl implements DisplayContextParameterValidator {

    private DateTimeFormatParser dateTimeFormatParser;

    @Autowired
    public DateTimeDisplayValidatorImpl(DateTimeFormatParser dateTimeFormatParser) {
        this.dateTimeFormatParser = dateTimeFormatParser;
    }

    @Override
    public DisplayContextParameterType getType() {
        return DisplayContextParameterType.DATETIMEDISPLAY;
    }

    @Override
    public void validate(final String parameterValue, String fieldType) throws InvalidDateTimeFormatException {
        dateTimeFormatParser.parseDateTimeFormat(parameterValue);
    }
}
