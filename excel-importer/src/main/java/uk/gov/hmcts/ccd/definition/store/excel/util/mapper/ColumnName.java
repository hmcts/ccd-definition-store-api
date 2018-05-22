package uk.gov.hmcts.ccd.definition.store.excel.util.mapper;

import java.util.Arrays;
import java.util.List;

/**
 * Enum to track all valid column names for a Case Definition import
 */
public enum ColumnName {
    CALLBACK_URL_ABOUT_TO_START_EVENT("CallBackURLAboutToStartEvent"),
    CALLBACK_URL_ABOUT_TO_SUBMIT_EVENT("CallBackURLAboutToSubmitEvent"),
    CALLBACK_URL_SUBMITTED_EVENT("CallBackURLSubmittedEvent"),
    CASE_EVENT_ID("CaseEventID"),
    CASE_FIELD_ID("CaseFieldID"),
    CASE_TYPE_ID("CaseTypeID"),
    CHANNEL("Channel"),
    CRUD("CRUD"),
    DEFAULT_HIDDEN("DefaultHidden"),
    DESCRIPTION("Description"),
    DISPLAY_ORDER("DisplayOrder"),
    DISPLAY_CONTEXT("DisplayContext"),
    ELEMENT_LABEL("ElementLabel"),
    END_BUTTON_LABEL("EndButtonLabel"),
    FIELD_TYPE("FieldType"),
    FIELD_TYPE_PARAMETER("FieldTypeParameter"),
    HINT_TEXT("HintText"),
    ID("ID"),
    LABEL("Label"),
    LIST_ELEMENT_CODE("ListElementCode"),
    LIST_ELEMENT("ListElement"),
    LIVE_FROM("LiveFrom"),
    LIVE_TO("LiveTo"),
    MAX("Max"),
    MIN("Min"),
    NAME("Name"),
    PAGE_ID("PageID"),
    PAGE_LABEL("PageLabel"),
    PAGE_DISPLAY_ORDER("PageDisplayOrder"),
    PAGE_FIELD_DISPLAY_ORDER("PageFieldDisplayOrder"),
    PAGE_COLUMN("PageColumnNumber"),
    POST_CONDITION_STATE("PostConditionState"),
    PRE_CONDITION_STATE("PreConditionState(s)"),
    PRINTABLE_DOCUMENTS_URL("PrintableDocumentsUrl"),
    REGULAR_EXPRESSION("RegularExpression"),
    RETRIES_TIMEOUT_ABOUT_TO_START_EVENT("RetriesTimeoutAboutToStartEvent"),
    RETRIES_TIMEOUT_URL_ABOUT_TO_SUBMIT_EVENT("RetriesTimeoutURLAboutToSubmitEvent"),
    RETRIES_TIMEOUT_URL_SUBMITTED_EVENT("RetriesTimeoutURLSubmittedEvent"),
    SECURITY_CLASSIFICATION("SecurityClassification"),
    FIELD_SHOW_CONDITION("FieldShowCondition"),
    PAGE_SHOW_CONDITION("PageShowCondition"),
    TAB_SHOW_CONDITION("TabShowCondition"),
    SHOW_SUMMARY("ShowSummary"),
    SHOW_EVENT_NOTES("ShowEventNotes"),
    SHOW_SUMMARY_CHANGE_OPTION("ShowSummaryChangeOption"),
    SHOW_SUMMARY_CONTENT_OPTION("ShowSummaryContentOption"),
    STATE_ID("CaseStateID"),
    TAB_ID("TabID"),
    TAB_LABEL("TabLabel"),
    TAB_DISPLAY_ORDER("TabDisplayOrder"),
    TAB_FIELD_DISPLAY_ORDER("TabFieldDisplayOrder"),
    USER_IDAM_ID("UserIDAMId"),
    USER_ROLE("UserRole"),
    WORK_BASKET_DEFAULT_JURISDICTION("WorkBasketDefaultJurisdiction"),
    WORK_BASKET_DEFAULT_CASETYPE("WorkBasketDefaultCaseType"),
    WORK_BASKET_DEFAULT_STATE("WorkBasketDefaultState");

    private String name;

    ColumnName(String columnName) {
        this.name = columnName;
    }

    /**
     * Method to indicate whether a field is required for the given sheet and column
     *
     * @param sheetName  - Name of the sheet
     * @param columnName - Name of the column
     * @return true if required, false otherwise
     */
    public static boolean isRequired(SheetName sheetName, ColumnName columnName) {
        // @formatter:off
        switch (sheetName) {
            case JURISDICTION:
            case CASE_TYPE:
                return columnName.equals(ColumnName.ID) ||
                    columnName.equals(ColumnName.NAME);
            case CASE_EVENT:
                return columnName.equals(ColumnName.ID) ||
                       columnName.equals(ColumnName.NAME) ||
                       columnName.equals(ColumnName.CASE_TYPE_ID);
            case CASE_FIELD:
                return columnName.equals(ColumnName.ID) ||
                       columnName.equals(ColumnName.NAME) ||
                       columnName.equals(ColumnName.FIELD_TYPE) ||
                       columnName.equals(ColumnName.CASE_TYPE_ID);
            case STATE:
                return columnName.equals(ColumnName.ID);
            case CASE_EVENT_TO_FIELDS:
                return columnName.equals(ColumnName.CASE_FIELD_ID) ||
                       columnName.equals(ColumnName.CASE_TYPE_ID) ||
                       columnName.equals(ColumnName.CASE_EVENT_ID) ||
                       columnName.equals(ColumnName.PAGE_ID);
            case FIXED_LISTS:
                return columnName.equals(ColumnName.LIST_ELEMENT_CODE) ||
                    columnName.equals(ColumnName.LIST_ELEMENT);
            case COMPLEX_TYPES:
                return columnName.equals(ColumnName.ID) ||
                       columnName.equals(ColumnName.FIELD_TYPE);
            case SEARCH_INPUT_FIELD:
                return columnName.equals(ColumnName.CASE_TYPE_ID) ||
                       columnName.equals(ColumnName.CASE_FIELD_ID);
            case SEARCH_RESULT_FIELD:
                return columnName.equals(ColumnName.CASE_TYPE_ID) ||
                       columnName.equals(ColumnName.CASE_FIELD_ID);
            case WORK_BASKET_INPUT_FIELD:
                return columnName.equals(ColumnName.CASE_TYPE_ID) ||
                       columnName.equals(ColumnName.CASE_FIELD_ID);
            case WORK_BASKET_RESULT_FIELDS:
                return columnName.equals(ColumnName.CASE_TYPE_ID) ||
                       columnName.equals(ColumnName.CASE_FIELD_ID);
            case CASE_TYPE_TAB:
                return columnName.equals(ColumnName.CASE_TYPE_ID) ||
                       columnName.equals(ColumnName.TAB_ID) ||
                       columnName.equals(ColumnName.CASE_FIELD_ID);
            case AUTHORISATION_CASE_TYPE:
                return columnName.equals(ColumnName.CASE_TYPE_ID) ||
                       columnName.equals(ColumnName.USER_ROLE) ||
                       columnName.equals(ColumnName.CRUD);
            case AUTHORISATION_CASE_FIELD:
                return columnName.equals(ColumnName.CASE_TYPE_ID) ||
                       columnName.equals(ColumnName.CASE_FIELD_ID) ||
                       columnName.equals(ColumnName.USER_ROLE) ||
                       columnName.equals(ColumnName.CRUD);
            case AUTHORISATION_CASE_EVENT:
                return columnName.equals(ColumnName.CASE_TYPE_ID) ||
                       columnName.equals(ColumnName.CASE_EVENT_ID) ||
                       columnName.equals(ColumnName.USER_ROLE) ||
                       columnName.equals(ColumnName.CRUD);
            case AUTHORISATION_CASE_STATE:
                return columnName.equals(ColumnName.CASE_TYPE_ID) ||
                    columnName.equals(ColumnName.STATE_ID) ||
                    columnName.equals(ColumnName.USER_ROLE) ||
                    columnName.equals(ColumnName.CRUD);
            default:
                return false;
        // @formatter:on
        }
    }

    @Override
    public String toString() {
        return this.name;
    }
}
