package uk.gov.hmcts.ccd.definition.store.excel.validation;

import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.DESCRIPTION;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.FIELD_SHOW_CONDITION;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.ID;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.LABEL;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.LIST_ELEMENT_CODE;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.NAME;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_EVENT;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_EVENT_TO_COMPLEX_TYPES;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_ROLE;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_TYPE;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_TYPE_TAB;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.COMPLEX_TYPES;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.JURISDICTION;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.STATE;

/**
 * Enum to map column validation (maxLength) for a Case Definition import.
 */
public enum SpreadSheetValidationMappingEnum {
    JURISDICTION_ID(JURISDICTION.toString(), ID.toString(), 70),
    JURISDICTION_NAME(JURISDICTION.toString(), NAME.toString(), 30),
    JURISDICTION_DESCRIPTION(JURISDICTION.toString(), DESCRIPTION.toString(), 100),
    CASE_TYPE_ID(CASE_TYPE.toString(), ID.toString(), 70),
    CASE_TYPE_NAME(CASE_TYPE.toString(), NAME.toString(), 30),
    CASE_TYPE_DESCRIPTION(CASE_TYPE.toString(), DESCRIPTION.toString(), 100),

    CASE_FIELD_ID("CaseField", ID.toString(), 70),

    FIXED_LISTS_ID("FixedLists", ID.toString(), 70),
    FIXED_LISTS_LIST_ELEMENT_CODE("FixedLists", LIST_ELEMENT_CODE.toString(), 150),

    COMPLEX_TYPES_LIST_ELEMENT_CODE(COMPLEX_TYPES.toString(), LIST_ELEMENT_CODE.toString(), 70),
    COMPLEX_TYPES_ELEMENT_LABEL(COMPLEX_TYPES.toString(), "ElementLabel", 500),
    COMPLEX_TYPES_FIELD_SHOW_CONDITION(COMPLEX_TYPES.toString(), FIELD_SHOW_CONDITION.toString(), 1000),

    CASE_TYPE_TAB_ID(CASE_TYPE_TAB.toString(), ID.toString(), 70),

    EVENT_TO_COMPLEX_TYPES_LIST_ELEMENT_CODE(CASE_EVENT_TO_COMPLEX_TYPES.toString(), LIST_ELEMENT_CODE.toString(), 70),
    EVENT_TO_COMPLEX_TYPES_EVENT_ELEMENT_LABEL(CASE_EVENT_TO_COMPLEX_TYPES.toString(), "EventElementLabel", 500),
    EVENT_TO_COMPLEX_TYPES_FIELD_SHOW_CONDITION(CASE_EVENT_TO_COMPLEX_TYPES.toString(),
        FIELD_SHOW_CONDITION.toString(), 1000),

    CASE_TYPE_TAB_TAB_ID(CASE_TYPE_TAB.toString(), "TabID", 70),
    CASE_TYPE_TAB_TAB_LABEL(CASE_TYPE_TAB.toString(), "TabLabel", 200),
    CASE_TYPE_TAB_CHANNEL(CASE_TYPE_TAB.toString(), "Channel", 64),
    CASE_TYPE_TAB_LIST_ELEMENT_CODE(CASE_TYPE_TAB.toString(), LIST_ELEMENT_CODE.toString(), 300),
    CASE_TYPE_TAB_FIELD_SHOW_CONDITION(CASE_TYPE_TAB.toString(), FIELD_SHOW_CONDITION.toString(), 1000),

    STATE_ID(STATE.toString(), ID.toString(), 70),
    STATE_NAME(STATE.toString(), NAME.toString(), 100),
    STATE_DESCRIPTION(STATE.toString(), DESCRIPTION.toString(), 100),
    STATE_TITLE_DISPLAY(STATE.toString(), "TitleDisplay", 100),

    CASE_EVENT_ID(CASE_EVENT.toString(), ID.toString(), 70),
    CASE_EVENT_NAME(CASE_EVENT.toString(), NAME.toString(), 30),
    CASE_EVENT_DESCRIPTION(CASE_EVENT.toString(), DESCRIPTION.toString(), 100),
    CASE_EVENT_END_BUTTON_LABEL(CASE_EVENT.toString(), "EndButtonLabel", 200),

    CASE_EVENT_TO_FIELDS_FIELD_SHOW_CONDITION("CaseEventToFields", FIELD_SHOW_CONDITION.toString(), 1000),

    SEARCH_INPUT_FIELDS_LABEL("SearchInputFields", LABEL.toString(), 200),
    SEARCH_INPUT_FIELDS_CASE_FIELD_ELEMENT_PATH("SearchInputFields", LIST_ELEMENT_CODE.toString(), 300),

    SEARCH_RESULTS_FIELDS_LABEL("SearchResultFields", LABEL.toString(), 200),
    SEARCH_RESULTS_FIELDS_CASE_FIELD_ELEMENT_PATH("SearchResultFields", LIST_ELEMENT_CODE.toString(), 300),

    SEARCH_CASES_RESULT_FIELDS_LABEL("SearchCaseResults", LABEL.toString(), 200),
    SEARCH_CASES_RESULT_FIELDS_CASE_FIELD_ELEMENT_PATH("SearchCaseResults", LIST_ELEMENT_CODE.toString(), 300),

    WORK_BASKET_INPUT_FIELDS_LABEL("WorkBasketInputFields", LABEL.toString(), 200),
    WORK_BASKET_INPUT_FIELDS_CASE_FIELD_ELEMENT_PATH("WorkBasketInputFields", LIST_ELEMENT_CODE.toString(), 300),

    WORK_BASKET_RESULT_FIELDS_LABEL("WorkBasketResultFields", LABEL.toString(), 200),
    WORK_BASKET_RESULT_FIELDS_CASE_FIELD_ELEMENT_PATH("WorkBasketResultFields", LIST_ELEMENT_CODE.toString(), 300),

    CASE_ROLES_ID(CASE_ROLE.toString(), ID.toString(), 255),
    CASE_ROLES_NAME(CASE_ROLE.toString(), NAME.toString(), 255),
    CASE_ROLES_DESCRIPTION(CASE_ROLE.toString(), DESCRIPTION.toString(), 255);

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
