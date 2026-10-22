package uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception;

import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;

/** Import diagnostics stay private unless created through a fixed validation-message factory. */
public class InvalidImportException extends RuntimeException {
    public static final String FIELD_REFERENCE_MESSAGE = "%sTab Invalid value '%s' is not a valid %s value. "
        + "The expression dot notation values should be valid caseTypes fields.";
    public static final String COLLECTION_MESSAGE = "SearchPartyTab Invalid value '%s' "
        + "is not a valid SearchPartyCollectionFieldName value "
        + "as it does not reference a collection of a complex type.";

    private final String publicMessage;

    public InvalidImportException() {
        publicMessage = null;
    }

    public InvalidImportException(String message) {
        super(message);
        publicMessage = null;
    }

    private InvalidImportException(String message, Throwable cause) {
        super(message, cause);
        publicMessage = message;
    }

    public static InvalidImportException invalidReference(SheetName sheet, ColumnName column,
                                                         String expression, Throwable cause) {
        return new InvalidImportException(String.format(FIELD_REFERENCE_MESSAGE, sheet, expression, column), cause);
    }

    public static InvalidImportException invalidCollection(String expression) {
        return new InvalidImportException(String.format(COLLECTION_MESSAGE, expression), null);
    }

    public static InvalidImportException invalidCategory(SheetName sheet, String category, boolean invalidFieldType) {
        return new InvalidImportException(String.format("%sTab Invalid value '%s' is not a valid CategoryID value. %s",
            sheet, category, invalidFieldType ? "Category not permitted for this field type."
                : "Category cannot be found."), null);
    }

    public final String getPublicMessage() {
        return publicMessage;
    }
}
