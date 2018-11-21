package uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception thrown if import of a Case Definition sheet fails due to missing data.
 *
 * @author Daniel Lam (A533913)
 */
public class InvalidImportException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(InvalidImportException.class);

    public InvalidImportException() {
        // default constructor
    }

    public InvalidImportException(final String message) {
        super(message);
        logger.error(message);
    }
}
