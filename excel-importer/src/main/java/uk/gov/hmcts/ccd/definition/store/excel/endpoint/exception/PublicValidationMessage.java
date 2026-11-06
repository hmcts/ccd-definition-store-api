package uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception;

/** Only fixed validation factories may provide public text; null means private diagnostics. */
public interface PublicValidationMessage {
    enum Format {
        PLAIN_TEXT, CODED_JSON, IMPORT_JSON
    }

    String getPublicMessage();

    default Format getResponseFormat() {
        return Format.PLAIN_TEXT;
    }
}
