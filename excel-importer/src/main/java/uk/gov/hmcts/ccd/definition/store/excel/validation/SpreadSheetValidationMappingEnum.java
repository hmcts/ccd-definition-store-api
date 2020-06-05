package uk.gov.hmcts.ccd.definition.store.excel.validation;

/**
 * Enum to map column validation (maxLength) for a Case Definition import.
 */
public enum SpreadSheetValidationMappingEnum {
    JURISDICTION_ID("Jurisdiction", "ID", 70),
    JURISDICTION_NAME("Jurisdiction", "Name", 30),
    JURISDICTION_DESCRIPTION("Jurisdiction", "Description", 100),
    CASE_TYPE_ID("CaseType", "ID", 70),
    CASE_TYPE_NAME("CaseType", "Name", 30),
    CASE_TYPE_DESCRIPTION("CaseType", "Description", 100),

    CASE_FIELD_ID("CaseField", "ID", 70),

    FIXED_LISTS_ID("FixedLists", "ID", 70),
    FIXED_LISTS_LIST_ELEMENT_CODE("FixedLists", "ListElementCode", 150),

    COMPLEX_TYPES_LIST_ELEMENT_CODE("ComplexTypes", "ListElementCode", 70),
    COMPLEX_TYPES_ELEMENT_LABEL("ComplexTypes", "ElementLabel", 200),
    COMPLEX_TYPES_FIELD_SHOW_CONDITION("ComplexTypes", "FieldShowCondition", 1000),

    CASE_TYPE_TAB_ID("CaseTypeTab", "ID", 70),

    EVENT_TO_COMPLEX_TYPES_LIST_ELEMENT_CODE("EventToComplexTypes", "ListElementCode", 70),
    EVENT_TO_COMPLEX_TYPES_EVENT_ELEMENT_LABEL("EventToComplexTypes", "EventElementLabel", 200),
    EVENT_TO_COMPLEX_TYPES_FIELD_SHOW_CONDITION("EventToComplexTypes", "FieldShowCondition", 1000),

    CASE_TYPE_TAB_TAB_ID("CaseTypeTab", "TabID", 70),
    CASE_TYPE_TAB_TAB_LABEL("CaseTypeTab", "TabLabel", 200),
    CASE_TYPE_TAB_CHANNEL("CaseTypeTab", "Channel", 64),
    CASE_TYPE_TAB_FIELD_SHOW_CONDITION("CaseTypeTab", "FieldShowCondition", 1000),

    STATE_ID("State", "ID", 70),
    STATE_NAME("State", "Name", 100),
    STATE_DESCRIPTION("State", "Description", 100),
    STATE_TITLE_DISPLAY("State", "TitleDisplay", 100),

    CASE_EVENT_ID("CaseEvent", "ID", 70),
    CASE_EVENT_NAME("CaseEvent", "Name", 30),
    CASE_EVENT_DESCRIPTION("CaseEvent", "Description", 100),
    CASE_EVENT_END_BUTTON_LABEL("CaseEvent", "EndButtonLabel", 200),

    CASE_EVENT_TO_FIELDS_FIELD_SHOW_CONDITION("CaseEventToFields", "FieldShowCondition", 1000),

    SEARCH_INPUT_FIELDS_LABEL("SearchInputFields", "Label", 200),
    SEARCH_INPUT_FIELDS_CASE_FIELD_ELEMENT_PATH("SearchInputFields", "ListElementCode", 300),

    SEARCH_RESULTS_FIELDS_LABEL("SearchResultFields", "Label", 200),
    SEARCH_RESULTS_FIELDS_CASE_FIELD_ELEMENT_PATH("SearchResultFields", "ListElementCode", 300),

    SEARCH_CASES_RESULT_FIELDS_LABEL("SearchCaseResults", "Label", 200),
    SEARCH_CASES_RESULT_FIELDS_CASE_FIELD_ELEMENT_PATH("SearchCaseResults", "ListElementCode", 300),

    WORK_BASKET_INPUT_FIELDS_LABEL("WorkBasketInputFields", "Label", 30),
    WORK_BASKET_INPUT_FIELDS_CASE_FIELD_ELEMENT_PATH("WorkBasketInputFields", "ListElementCode", 300),

    WORK_BASKET_RESULT_FIELDS_LABEL("WorkBasketResultFields", "Label", 200),
    WORK_BASKET_RESULT_FIELDS_CASE_FIELD_ELEMENT_PATH("WorkBasketResultFields", "ListElementCode", 300),

    CASE_ROLES_ID("CaseRoles", "ID", 255),
    CASE_ROLES_NAME("CaseRoles", "Name", 255),
    CASE_ROLES_DESCRIPTION("CaseRoles", "Description", 255);

    private String sheetName;
    private String sheetColumnName;
    private Integer maxLength;

    SpreadSheetValidationMappingEnum(String sheetName, String sheetColumnName, Integer maxLength) {
        this.sheetName = sheetName;
        this.sheetColumnName = sheetColumnName;
        this.maxLength = maxLength;
    }

    public static SpreadSheetValidationMappingEnum fromSheetColumnName(String sheetName, String sheetColumnName) {
        for (SpreadSheetValidationMappingEnum sheetColumnEnum : SpreadSheetValidationMappingEnum.values()) {
            if (sheetColumnEnum.sheetName.equals(sheetName)
                && sheetColumnEnum.sheetColumnName.equals(sheetColumnName)) {
                return sheetColumnEnum;
            }
        }
        return null;
    }

    public Integer getMaxLength() {
        return maxLength;
    }
}
