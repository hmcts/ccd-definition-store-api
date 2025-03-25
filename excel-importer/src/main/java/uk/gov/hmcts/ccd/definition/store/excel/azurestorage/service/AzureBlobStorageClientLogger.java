package uk.gov.hmcts.ccd.definition.store.excel.azurestorage.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AzureBlobStorageClientLogger {

    private final Logger LOG = LoggerFactory.getLogger(AzureBlobStorageClientLogger.class);

    public void jclog(final String message) {
        LOG.info("JCDEBUG: info: AzureBlobStorageClient: " + message);
        LOG.warn("JCDEBUG: warn: AzureBlobStorageClient: " + message);
        LOG.error("JCDEBUG: error: AzureBlobStorageClient: " + message);
        LOG.debug("JCDEBUG: debug: AzureBlobStorageClient: " + message);
    }
}
