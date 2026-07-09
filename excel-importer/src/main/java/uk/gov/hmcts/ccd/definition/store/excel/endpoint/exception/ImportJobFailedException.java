package uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception;

import java.util.UUID;

public class ImportJobFailedException extends RuntimeException {

    private final UUID jobId;

    public ImportJobFailedException(UUID jobId, Throwable cause) {
        super(cause);
        this.jobId = jobId;
    }

    public UUID getJobId() {
        return jobId;
    }
}
