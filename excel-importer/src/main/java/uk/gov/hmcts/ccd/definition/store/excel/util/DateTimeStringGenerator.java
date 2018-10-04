package uk.gov.hmcts.ccd.definition.store.excel.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class DateTimeStringGenerator {

    /**
     * Generate a date/time string with the current time in UTC.
     *
     * @return The date/time, in the format <code>yyyyMMddHHmmss</code>
     */
    public String generateCurrentDateTime() {
        LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("UTC"));
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}
