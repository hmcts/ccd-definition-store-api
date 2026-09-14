package uk.gov.hmcts.ccd.definition.store.excel.util;

public class ReferenceUtils {

    private ReferenceUtils() {
        // Hide Utility Class Constructor : Utility classes should not have a public or default constructor
        // (squid:S1118)
    }

    public static String listReference(String listBaseType, String listId) {
        return String.format("%s-%s", listBaseType, listId);
    }

    /**
     * Identifier of a list type declared against a single case type, i.e. a FixedLists row with a CaseTypeID.
     * Lists sharing an ID across case types are scoped this way so that each case type gets its own field type
     * rather than a single one shared across the jurisdiction.
     */
    public static String caseTypeScopedListId(String listId, String caseTypeId) {
        return String.format("%s-%s", listId, caseTypeId);
    }

}
