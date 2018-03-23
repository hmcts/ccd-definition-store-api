package uk.gov.hmcts.ccd.definition.store.rest.endpoint.exceptions;

public class DuplicateFoundException extends RuntimeException {

    public DuplicateFoundException() {
        // default constructor
    }

    public DuplicateFoundException(String message) {
        super(message);

    }

}
