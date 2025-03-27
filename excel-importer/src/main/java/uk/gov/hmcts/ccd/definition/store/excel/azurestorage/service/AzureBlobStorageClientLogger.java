package uk.gov.hmcts.ccd.definition.store.excel.azurestorage.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AzureBlobStorageClientLogger {

    public static Logger getLogger() {
        try {
            return LoggerFactory.getLogger(AzureBlobStorageClientLogger.class);
        } catch (IllegalAccessError e) {
            return LoggerFactory.getLogger(AzureBlobStorageClientLogger.class);
        }
    }

}
