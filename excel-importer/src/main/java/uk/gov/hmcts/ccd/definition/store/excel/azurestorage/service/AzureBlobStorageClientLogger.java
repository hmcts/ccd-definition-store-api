package uk.gov.hmcts.ccd.definition.store.excel.azurestorage.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AzureBlobStorageClientLogger {

    private final Logger log = LoggerFactory.getLogger(AzureBlobStorageClientLogger.class);

    public void jclog(final String message) {
        log.info("JCDEBUG: info: AzureBlobStorageClient: " + message);
        log.warn("JCDEBUG: warn: AzureBlobStorageClient: " + message);
        log.error("JCDEBUG: error: AzureBlobStorageClient: " + message);
        log.debug("JCDEBUG: debug: AzureBlobStorageClient: " + message);
    }
}
