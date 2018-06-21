package uk.gov.hmcts.ccd.definition.store.excel.util.mapper;

/**
 * Enum to track all valid Sheet names for a Case Definition import
 */
public enum SheetName {
    JURISDICTION("Jurisdiction"),
    CASE_TYPE("CaseType"),
    CASE_FIELD("CaseField"),
    STATE("State"),
    CASE_EVENT("CaseEvent"),
    CASE_EVENT_TO_FIELDS("CaseEventToFields", true),
    WORK_BASKET_INPUT_FIELD("WorkBasketInputFields"),
    WORK_BASKET_RESULT_FIELDS("WorkBasketResultFields", true),
    CASE_TYPE_TAB("CaseTypeTab", true),
    SEARCH_INPUT_FIELD("SearchInputFields", true),
    SEARCH_RESULT_FIELD("SearchResultFields", true),
    USER_PROFILE("UserProfile"),
    FIXED_LISTS("FixedLists"),
    COMPLEX_TYPES("ComplexTypes"),
    AUTHORISATION_CASE_TYPE("AuthorisationCaseType"),
    AUTHORISATION_CASE_FIELD("AuthorisationCaseField"),
    AUTHORISATION_CASE_EVENT("AuthorisationCaseEvent"),
    AUTHORISATION_CASE_STATE("AuthorisationCaseState");

    private final String name;
    private boolean allowMetaDataFields;

    SheetName(String sheetName) {
        this.name = sheetName;
    }

    SheetName(String name, boolean allowMetaDataFields) {
        this.name = name;
        this.allowMetaDataFields = allowMetaDataFields;
    }

    public String getName() {
        return name;
    }

    public boolean isAllowMetaDataFields() {
        return allowMetaDataFields;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static SheetName forName(String name) {
        for (SheetName sheetName : SheetName.values()) {
            if (name.equals(sheetName.getName())) {
                return sheetName;
            }
        }

        return null;
    }
}
