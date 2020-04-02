package uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.datetime.DateTimeFormatParser;
import uk.gov.hmcts.ccd.definition.store.domain.datetime.InvalidDateTimeFormatException;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;

import java.util.regex.*;

import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_DATE;

@Component
public class DateTimeDisplayValidatorImpl implements DisplayContextParameterValidator {

    private DateTimeFormatParser dateTimeFormatParser;

    private static final Pattern DEFAULT_PATTERN = Pattern.compile(".+");
    private static final Pattern ALLOWED_CHARACTERS_PATTERN_DATE = Pattern.compile("[yMd\\-\\/ ':]+");

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
        Pattern pattern = ((fieldType.equals(BASE_DATE)) ? ALLOWED_CHARACTERS_PATTERN_DATE : DEFAULT_PATTERN);
        dateTimeFormatParser.parseDateTimeFormat(parameterValue, pattern);
    }
}
