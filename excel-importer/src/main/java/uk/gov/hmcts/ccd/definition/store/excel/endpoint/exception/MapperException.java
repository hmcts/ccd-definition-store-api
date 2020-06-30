package uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception;

/**
 * Exception thrown when the mapping of a Case Definition fails.
 */
public class MapperException extends RuntimeException {
    public MapperException(String message) {
        super(message);
    }

    public MapperException(String message, Throwable error) {
        super(message, error);
    }
}
