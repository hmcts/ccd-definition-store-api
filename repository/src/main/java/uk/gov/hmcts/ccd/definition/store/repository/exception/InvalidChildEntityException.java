package uk.gov.hmcts.ccd.definition.store.repository.exception;

public class InvalidChildEntityException extends RuntimeException {
    public InvalidChildEntityException(String message) {
        super(message);
    }
}
