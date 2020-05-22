package uk.gov.hmcts.ccd.definition.store.domain.datetime;

public class InvalidDateTimeFormatException extends Exception {

    private String dateTimeFormat;

    public InvalidDateTimeFormatException(String dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
    }

    public String getDateTimeFormat() {
        return this.dateTimeFormat;
    }
}
