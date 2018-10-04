package uk.gov.hmcts.ccd.definition.store.excel.azurestorage.exception;

public class FileStorageException extends RuntimeException {

    public FileStorageException(Exception e) {
        super(e);
    }

    public FileStorageException(String message) {
        super(message);
    }
}
