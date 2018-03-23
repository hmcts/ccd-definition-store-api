package uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception;

/**
 * Exception thrown if import of a Case Definition sheet fails due to missing data.
 *
 * @author Daniel Lam (A533913)
 */
public class InvalidImportException extends RuntimeException {

    public InvalidImportException() {
        // default constructor
    }

    public InvalidImportException(final String message) {
        super(message);
    }
}
