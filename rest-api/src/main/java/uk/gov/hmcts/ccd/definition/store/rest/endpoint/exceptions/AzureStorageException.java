package uk.gov.hmcts.ccd.definition.store.rest.endpoint.exceptions;

public class AzureStorageException extends RuntimeException {

    public AzureStorageException(String message, Throwable t) {
        super(message, t);
    }
}
