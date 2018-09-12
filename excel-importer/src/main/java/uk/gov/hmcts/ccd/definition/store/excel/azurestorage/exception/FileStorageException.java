package uk.gov.hmcts.ccd.definition.store.excel.azurestorage.exception;

public class FileStorageException extends RuntimeException {
    public static final String FILE_ALREADY_EXISTS_ERROR = "File already exists in the Azure storage container";

    public FileStorageException(Exception e) {
        super(e);
    }

    public FileStorageException(String message) {
        super(message);
    }
}
