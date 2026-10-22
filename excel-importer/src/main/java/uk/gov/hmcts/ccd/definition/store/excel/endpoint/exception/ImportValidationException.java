package uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception;

import java.util.List;
import java.util.stream.Collectors;

/** Explicitly approved validation text; arbitrary mapper exception messages stay private. */
public class ImportValidationException extends MapperException {
    public enum Reason {
        MISSING_ATTRIBUTE_HEADERS("Invalid Case Definition sheet - no Definition data attribute headers found"),
        MISSING_WORKBASKET_RESULT_FIELDS("A definition must contain a WorkBasketResultFields sheet"),
        MISSING_CASE_FIELDS("At least one case field must be defined for case type."),
        MISSING_LAYOUT_FIELDS("At least one layout case field must be defined for the case type and layout.");

        private final String message;

        Reason(String message) {
            this.message = message;
        }
    }

    private final String publicMessage;
    private final boolean plainText;

    public ImportValidationException(Reason reason) {
        this(reason, reason.message);
    }

    /** Diagnostic context is logged; only the approved reason is returned publicly. */
    public ImportValidationException(Reason reason, String diagnosticMessage) {
        super(diagnosticMessage);
        publicMessage = reason.message;
        plainText = false;
    }

    private ImportValidationException(String message) {
        super(message);
        publicMessage = message;
        plainText = true;
    }

    public static ImportValidationException invalidHiddenField(String field) {
        return new ImportValidationException(String.format("'retainHiddenValue' has been incorrectly configured or "
            + "is invalid for fieldID ['%s'] on ['CaseEventToFields']", field));
    }

    public static ImportValidationException hiddenFieldWithoutShowCondition(String field, boolean complex) {
        return new ImportValidationException(String.format("'retainHiddenValue'%s can only be configured for a field "
            + "that uses a showCondition. Field ['%s'] on ['CaseEventToFields'] does not use a showCondition",
            complex ? " on CaseEventToComplexTypes" : "", field));
    }

    public static ImportValidationException invalidRetainHiddenValue(Object value, String sheet) {
        return new ImportValidationException(String.format(
            "Invalid value '%s' is found in column 'RetainHiddenValue' in the sheet '%s'", value, sheet));
    }

    public static ImportValidationException combine(List<ImportValidationException> failures) {
        ImportValidationException combined = new ImportValidationException(failures.stream()
            .map(ImportValidationException::getPublicMessage).collect(Collectors.joining("\n")));
        failures.forEach(combined::addSuppressed);
        return combined;
    }

    public boolean isPlainText() {
        return plainText;
    }

    public String getPublicMessage() {
        return publicMessage;
    }
}
