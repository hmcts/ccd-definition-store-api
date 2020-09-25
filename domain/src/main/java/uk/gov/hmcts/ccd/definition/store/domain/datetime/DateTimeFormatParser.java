package uk.gov.hmcts.ccd.definition.store.domain.datetime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class DateTimeFormatParser {

    public void parseDateTimeFormat(String dateTimeFormat, Pattern dateTimeFormatPattern)
        throws InvalidDateTimeFormatException {
        Matcher m = dateTimeFormatPattern.matcher(dateTimeFormat);
        if (m.find()) {
            throw new InvalidDateTimeFormatException(dateTimeFormat);
        }
        try {
            DateTimeFormatter.ofPattern(dateTimeFormat);
        } catch (IllegalArgumentException e) {
            log.error("Error occurred while parsing date time format " + dateTimeFormat, e);
            throw new InvalidDateTimeFormatException(dateTimeFormat);
        }
    }
}
