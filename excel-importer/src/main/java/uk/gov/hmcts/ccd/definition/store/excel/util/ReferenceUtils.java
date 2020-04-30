package uk.gov.hmcts.ccd.definition.store.excel.util;

public class ReferenceUtils {

    private ReferenceUtils() {
        // Hide Utility Class Constructor : Utility classes should not have a public or default constructor (squid:S1118)
    }

    public static String listReference(String listBaseType, String listId) {
        return String.format("%s-%s", listBaseType, listId);
    }

}
