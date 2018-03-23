package uk.gov.hmcts.ccd.definition.store.excel.util;

public class ReferenceUtils {

    public static String listReference(String listBaseType, String listId) {
        return String.format("%s-%s", listBaseType, listId);
    }

}
