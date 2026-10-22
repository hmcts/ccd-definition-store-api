package uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception;

/** Explicitly approved validation text; arbitrary mapper exception messages stay private. */
public class ImportValidationException extends MapperException {
    public enum Reason {
        MISSING_ATTRIBUTE_HEADERS("Invalid Case Definition sheet - no Definition data attribute headers found"),
        MISSING_WORKBASKET_RESULT_FIELDS("A definition must contain a WorkBasketResultFields sheet"),
        MISSING_CASE_FIELDS("At least one case field must be defined for case type."),
        MISSING_LAYOUT_FIELDS("At least one layout case field must be defined for the case type and layout."),
        DUPLICATE_DISPLAY_ORDER("DisplayOrder cannot be duplicated within case type category and parent category"
            + " in Category tab");

        private final String message;

        Reason(String message) {
            this.message = message;
        }
    }

    private final Reason reason;

    public ImportValidationException(Reason reason) {
        this(reason, reason.message);
    }

    /** Diagnostic context is logged; only the approved reason is returned publicly. */
    public ImportValidationException(Reason reason, String diagnosticMessage) {
        super(diagnosticMessage);
        this.reason = reason;
    }

    public String getPublicMessage() {
        return reason.message;
    }
}
