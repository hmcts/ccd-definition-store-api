package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * validation done in domain layer.
 * @deprecated Validation is to be done in the domain layer
 */
@Deprecated
@ResponseStatus(HttpStatus.BAD_REQUEST)
// It appears this class is mainly being used for validation errors (invalid data) as opposed to parsing errors (invalid format)
// Validation should be done in the domain; parsers should only throw an exception if there's a very good reason for it
public class SpreadsheetParsingException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(SpreadsheetParsingException.class);

    public SpreadsheetParsingException(String message) {
        super(message);
        logger.warn(message);
    }
}
