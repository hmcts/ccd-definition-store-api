package uk.gov.hmcts.ccd.definition.store.domain.datetime;

import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DateTimeFormatParser {

    private static final Logger LOG = LoggerFactory.getLogger(DateTimeFormatParser.class);

    private static final Pattern DEFAULT_PATTERN = Pattern.compile(".+");

    public void parseDateTimeFormat(String dateTimeFormat) throws InvalidDateTimeFormatException {
        parseDateTimeFormat(dateTimeFormat, DEFAULT_PATTERN);
    }

    public void parseDateTimeFormat(String dateTimeFormat, Pattern dateTimeFormatPattern) throws InvalidDateTimeFormatException {
        Matcher m = dateTimeFormatPattern.matcher(dateTimeFormat);
        if (!m.matches()) {
            throw new InvalidDateTimeFormatException(dateTimeFormat);
        }
        try {
            DateTimeFormatter.ofPattern(dateTimeFormat);
        } catch (IllegalArgumentException e) {
            LOG.error("Error occurred while parsing date time format " + dateTimeFormat, e);
            throw new InvalidDateTimeFormatException(dateTimeFormat);
        }
    }
}
