package uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception;

import java.util.List;
import java.util.stream.Collectors;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;

/** Explicitly approved validation text; arbitrary mapper exception messages stay private. */
public class ImportValidationException extends MapperException implements PublicValidationMessage {
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
    private final Format responseFormat;

    /** Fixed templates for actionable spreadsheet rules; never accept exception diagnostics here. */
    public enum Rule {
        NULLIFY_WITH_DEFAULT("NullifyByDefault cannot be set to Yes if DefaultValue has a value in CaseEventToFields"),
        PUBLISH_ON_COMPLEX("Publish column must not be set for case field '%s', event '%s' in CaseEventToFields "
            + "when DisplayContext is COMPLEX. Please only use the Publish overrides in EventToComplexTypes."),
        JURISDICTION_COUNT("A definition must contain exactly one Jurisdiction"),
        MISSING_CASE_TYPE("A definition must contain at least one Case Type"),
        MISSING_CASE_FIELD_SHEET("A definition must contain a Case Field worksheet"),
        MISSING_COMPLEX_TYPES_SHEET("A definition must contain a Complex Types worksheet"),
        MISSING_FIXED_LIST_SHEET("A definition must contain a Fixed List worksheet"),
        MISSING_DISPLAY_GROUP_SHEET("A definition must contain a %s sheet with at least one entry"),
        MISSING_TAB_FIELDS("At least one CaseField must be defined in the CaseTypeTab for case type %s"),
        DUPLICATE_LAYOUT_ROW("Please make sure each row in worksheet %s is unique for case type %s"),
        BLANK_REQUIRED_COLUMN("There's a missing value in the column '%s' or invalid value in the sheet '%s'"),
        UNKNOWN_LAYOUT_ACCESS_PROFILE("- Unknown access profile '%s' in worksheet '%s' for caseField '%s'"),
        INVALID_RESULTS_ORDERING("Invalid results ordering pattern '%s' in worksheet '%s' "
            + "for caseType '%s' for caseField '%s'"),
        MISSING_SORT_PRIORITY("Missing sort order priority in worksheet '%s' for caseType '%s'"),
        DUPLICATE_SORT_PRIORITY("Duplicate sort order priority in worksheet '%s' for caseType '%s'"),
        MULTIPLE_TAB_ACCESS_PROFILES("Please provide one access profile row per tab in worksheet %s "
            + "on column ACCESS_PROFILE for the tab %s"),
        INVALID_TAB_ACCESS_PROFILE("- Invalid access profile or case role '%s' in '%s' tab for TabId '%s'"),
        MULTIPLE_TAB_CONDITIONS("Please provide single condition in %s column in %s for the tab %s"),
        MISSING_MAPPER_VALUE("There's a missing value in column %s in the sheet %s"),
        BLANK_MAPPER_VALUE("Couldn't find the column '%s' or invalid value in the sheet '%s'"),
        DUPLICATE_EVENT("Event already registered for ID: %s");

        private final String template;

        Rule(String template) {
            this.template = template;
        }
    }

    public static ImportValidationException rule(Rule rule, String... values) {
        return new ImportValidationException(String.format(rule.template, (Object[]) values));
    }

    public ImportValidationException(Reason reason) {
        this(reason, reason.message);
    }

    /** Diagnostic context is logged; only the approved reason is returned publicly. */
    public ImportValidationException(Reason reason, String diagnosticMessage) {
        super(diagnosticMessage);
        publicMessage = reason.message;
        responseFormat = Format.CODED_JSON;
    }

    private ImportValidationException(String message) {
        this(message, null);
    }

    private ImportValidationException(String message, Throwable cause) {
        super(message, cause);
        publicMessage = message;
        responseFormat = Format.PLAIN_TEXT;
    }

    public static ImportValidationException invalidIntegerList(String value, ColumnName column,
                                                                String sheet, NumberFormatException cause) {
        return new ImportValidationException(String.format(
            "Found '%s' whilst expecting a comma separated list of numbers in the column '%s' of sheet '%s'",
            value, column, sheet), cause);
    }

    public static ImportValidationException multipleDefaultFocus(String sheet, long count, String caseType) {
        return new ImportValidationException(String.format(
            "Only one row per case type can have DefaultFocus set to true in worksheet %s; "
                + "found %d for case type %s", sheet, count, caseType));
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

    public static ImportValidationException invalidValue(Object value, ColumnName column, String sheet) {
        return new ImportValidationException(String.format(
            "Invalid value '%s' is found in column '%s' in the sheet '%s'", value, column, sheet));
    }

    public static ImportValidationException missingColumn(ColumnName column, String sheet) {
        return new ImportValidationException(String.format("Couldn't find the column %s in the sheet %s",
            column, sheet));
    }

    public static ImportValidationException combine(List<ImportValidationException> failures) {
        ImportValidationException combined = new ImportValidationException(failures.stream()
            .map(ImportValidationException::getPublicMessage).collect(Collectors.joining("\n")));
        failures.forEach(combined::addSuppressed);
        return combined;
    }

    @Override
    public Format getResponseFormat() {
        return responseFormat;
    }

    public String getPublicMessage() {
        return publicMessage;
    }
}
