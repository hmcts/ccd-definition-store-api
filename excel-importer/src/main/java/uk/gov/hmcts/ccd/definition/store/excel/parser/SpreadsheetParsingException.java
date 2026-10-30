package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.PublicValidationMessage;

/**
 * Spreadsheet parsing failure with optional, explicitly approved validation feedback.
 *
 * @deprecated Validation is to be done in the domain layer.
 */
@Deprecated
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SpreadsheetParsingException extends RuntimeException implements PublicValidationMessage {
    private final String publicMessage;

    public enum DuplicateDefinition {
        FIELD_TYPE("Type already registered for field: "),
        STATE("State already registered for ID: "),
        CATEGORY("Category already registered for ID: "),
        CASE_FIELD("Case field already registered for ID: ");

        private final String prefix;

        DuplicateDefinition(String prefix) {
            this.prefix = prefix;
        }
    }

    public static SpreadsheetParsingException duplicate(DuplicateDefinition definition, String id) {
        return new SpreadsheetParsingException(definition.prefix + id, null);
    }

    public enum ReferenceValidation {
        MISSING_CASE_TYPES("No types registered for case type: %s"),
        MISSING_FIELD_TYPE("No types registered for case field ID: %s/%s"),
        MISSING_STATES("No states registered for case type: %s"),
        MISSING_CASE_ROLES("No case roles registered for case type: %s"),
        UNKNOWN_CASE_ROLE("No case role registered for case role ID: %s/%s"),
        MISSING_CASE_FIELDS("No case fields registered for case type: %s"),
        UNKNOWN_FIELD("Unknown field %s for case type %s"),
        UNDEFINED_FIELD_TYPE("Missing field type: %s"),
        UNDEFINED_COLLECTION_TYPE("No type found for collection of: %s"),
        REQUIRED_STATE("At least one state must be defined for case type: %s");

        private final String template;

        ReferenceValidation(String template) {
            this.template = template;
        }
    }

    public static SpreadsheetParsingException reference(ReferenceValidation rule, String... values) {
        return new SpreadsheetParsingException(String.format(rule.template, (Object[]) values), null);
    }

    public SpreadsheetParsingException(String message) {
        super(message);
        publicMessage = null;
    }

    private SpreadsheetParsingException(String message, Throwable cause) {
        super(message, cause);
        publicMessage = message;
    }

    public static SpreadsheetParsingException invalidPostState(String entry) {
        return new SpreadsheetParsingException("Invalid Post State " + entry, null);
    }

    public static SpreadsheetParsingException invalidCondition(String entry, Throwable cause) {
        return new SpreadsheetParsingException("Invalid Post State Condition " + entry, cause);
    }

    public static SpreadsheetParsingException missingState(String caseType, String state) {
        return new SpreadsheetParsingException("No state registered for state ID: " + caseType + "/" + state, null);
    }

    public final String getPublicMessage() {
        return publicMessage;
    }

    @Override
    public Format getResponseFormat() {
        return Format.IMPORT_JSON;
    }
}
