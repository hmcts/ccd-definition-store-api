package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Spreadsheet parsing failure with optional, explicitly approved validation feedback.
 *
 * @deprecated Validation is to be done in the domain layer.
 */
@Deprecated
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SpreadsheetParsingException extends RuntimeException {
    private final String publicMessage;

    public SpreadsheetParsingException(String message) {
        super(message);
        publicMessage = null;
    }

    private SpreadsheetParsingException(String message, Throwable cause) {
        super(message, cause);
        publicMessage = message;
    }

    public static SpreadsheetParsingException invalidPostState(String entry) {
        return new SpreadsheetParsingException("Invalid Post State " + entry, null);
    }

    public static SpreadsheetParsingException invalidCondition(String entry, Throwable cause) {
        return new SpreadsheetParsingException("Invalid Post State Condition " + entry, cause);
    }

    public static SpreadsheetParsingException missingState(String caseType, String state) {
        return new SpreadsheetParsingException("No state registered for state ID: " + caseType + "/" + state, null);
    }

    public final String getPublicMessage() {
        return publicMessage;
    }
}
