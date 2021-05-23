package uk.gov.hmcts.ccd.definition.store.excel.util.mapper;

/**
 * Enum to track all valid column names for a Case Definition import.
 */
public enum ColumnName {
    CALLBACK_URL_ABOUT_TO_START_EVENT("CallBackURLAboutToStartEvent"),
    CALLBACK_URL_ABOUT_TO_SUBMIT_EVENT("CallBackURLAboutToSubmitEvent"),
    CALLBACK_URL_SUBMITTED_EVENT("CallBackURLSubmittedEvent"),
    CALLBACK_URL_MID_EVENT("CallBackURLMidEvent"),
    CAN_SAVE_DRAFT("CanSaveDraft"),
    CASE_EVENT_ID("CaseEventID"),
    CASE_EVENT_FIELD_LABEL("CaseEventFieldLabel"),
    CASE_EVENT_FIELD_HINT("CaseEventFieldHint"),
    CASE_FIELD_ID("CaseFieldID"),
    CASE_TYPE_ID("CaseTypeID"),
    CHANNEL("Channel"),
    CRUD("CRUD"),
    DEFAULT_HIDDEN("DefaultHidden"),
    DESCRIPTION("Description"),
    DISPLAY_ORDER("DisplayOrder"),
    FIELD_DISPLAY_ORDER("FieldDisplayOrder"),
    DISPLAY_CONTEXT("DisplayContext"),
    DISPLAY_CONTEXT_PARAMETER("DisplayContextParameter"),
    ELEMENT_LABEL("ElementLabel"),
    EVENT_ELEMENT_LABEL("EventElementLabel"),
    END_BUTTON_LABEL("EndButtonLabel"),
    FIELD_TYPE("FieldType"),
    FIELD_TYPE_PARAMETER("FieldTypeParameter"),
    HINT_TEXT("HintText"),
    EVENT_HINT_TEXT("EventHintText"),
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
    RETRIES_TIMEOUT_URL_MID_EVENT("RetriesTimeoutURLMidEvent"),
    SEARCHABLE("Searchable"),
    SECURITY_CLASSIFICATION("SecurityClassification"),
    FIELD_SHOW_CONDITION("FieldShowCondition"),
    PAGE_SHOW_CONDITION("PageShowCondition"),
    TAB_SHOW_CONDITION("TabShowCondition"),
    SEARCH_ALIAS_ID("SearchAliasID"),
    SHUTTERED("Shuttered"),
    SHOW_SUMMARY("ShowSummary"),
    PUBLISH("Publish"),
    PUBLISH_AS("PublishAs"),
    SHOW_EVENT_NOTES("ShowEventNotes"),
    SHOW_SUMMARY_CHANGE_OPTION("ShowSummaryChangeOption"),
    SHOW_SUMMARY_CONTENT_OPTION("ShowSummaryContentOption"),
    STATE_ID("CaseStateID"),
    TAB_ID("TabID"),
    TAB_LABEL("TabLabel"),
    TAB_DISPLAY_ORDER("TabDisplayOrder"),
    TAB_FIELD_DISPLAY_ORDER("TabFieldDisplayOrder"),
    TITLE_DISPLAY("TitleDisplay"),
    USER_IDAM_ID("UserIDAMId"),
    USER_ROLE("UserRole"),
    USE_CASE("UseCase"),
    RESULTS_ORDERING("ResultsOrdering"),
    WORK_BASKET_DEFAULT_JURISDICTION("WorkBasketDefaultJurisdiction"),
    WORK_BASKET_DEFAULT_CASETYPE("WorkBasketDefaultCaseType"),
    WORK_BASKET_DEFAULT_STATE("WorkBasketDefaultState"),
    BANNER_ENABLED("BannerEnabled"),
    BANNER_DESCRIPTION("BannerDescription"),
    BANNER_URL_TEXT("BannerURLText"),
    BANNER_URL("BannerURL"),
    DEFAULT_VALUE("DefaultValue"),
    RETAIN_HIDDEN_VALUE("RetainHiddenValue"),
    REASON_REQUIRED("ReasonRequired"),
    NOC_ACTION_INTERPRETATION_REQUIRED("NoCActionInterpretationRequired"),
    CHALLENGE_QUESTION_TEXT("QuestionText"),
    CHALLENGE_QUESTION_ANSWER_FIELD_TYPE("AnswerFieldType"),
    CHALLENGE_QUESTION_CASE_ROLE_ID("CaseRoleId"),
    CHALLENGE_QUESTION_QUESTION_ID("QuestionId"),
    CHALLENGE_QUESTION_ANSWER_FIELD("Answer"),
    ROLE_NAME("RoleName"),
    AUTHORISATION("Authorisation"),
    READ_ONLY("ReadOnly"),
    DISABLED("Disabled"),
    ACCESS_PROFILES("AccessProfiles"),
    EVENT_ENABLING_CONDITION("EventEnablingCondition");

    private final String name;

    ColumnName(String columnName) {
        this.name = columnName;
    }

    /**
     * Method to indicate whether a field is required for the given sheet and column.
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
                return columnName.equals(ColumnName.ID)
                    || columnName.equals(ColumnName.NAME);
            case CASE_EVENT:
                return columnName.equals(ColumnName.ID)
                    || columnName.equals(ColumnName.NAME)
                    || columnName.equals(ColumnName.CASE_TYPE_ID);
            case CASE_FIELD:
                return columnName.equals(ColumnName.ID)
                    || columnName.equals(ColumnName.NAME)
                    || columnName.equals(ColumnName.FIELD_TYPE)
                    || columnName.equals(ColumnName.CASE_TYPE_ID);
            case STATE:
                return columnName.equals(ColumnName.ID);
            case CASE_EVENT_TO_FIELDS:
                return columnName.equals(ColumnName.CASE_FIELD_ID)
                    || columnName.equals(ColumnName.CASE_TYPE_ID)
                    || columnName.equals(ColumnName.CASE_EVENT_ID)
                    || columnName.equals(ColumnName.PAGE_ID);
            case FIXED_LISTS:
                return columnName.equals(ColumnName.LIST_ELEMENT_CODE)
                    || columnName.equals(ColumnName.LIST_ELEMENT);
            case COMPLEX_TYPES:
                return columnName.equals(ColumnName.ID)
                    || columnName.equals(ColumnName.FIELD_TYPE);
            case SEARCH_INPUT_FIELD:
                return columnName.equals(ColumnName.CASE_TYPE_ID)
                    || columnName.equals(ColumnName.CASE_FIELD_ID);
            case SEARCH_RESULT_FIELD:
                return columnName.equals(ColumnName.CASE_TYPE_ID)
                    || columnName.equals(ColumnName.CASE_FIELD_ID);
            case WORK_BASKET_INPUT_FIELD:
                return columnName.equals(ColumnName.CASE_TYPE_ID)
                    || columnName.equals(ColumnName.CASE_FIELD_ID);
            case WORK_BASKET_RESULT_FIELDS:
                return columnName.equals(ColumnName.CASE_TYPE_ID)
                    || columnName.equals(ColumnName.CASE_FIELD_ID);
            case SEARCH_CASES_RESULT_FIELDS:
                return columnName.equals(ColumnName.CASE_TYPE_ID)
                    || columnName.equals(ColumnName.CASE_FIELD_ID)
                    || columnName.equals(ColumnName.USE_CASE);
            case CASE_TYPE_TAB:
                return columnName.equals(ColumnName.CASE_TYPE_ID)
                    || columnName.equals(ColumnName.TAB_ID)
                    || columnName.equals(ColumnName.CASE_FIELD_ID);
            case CASE_ROLE:
                return columnName.equals(CASE_TYPE_ID);
            case AUTHORISATION_CASE_TYPE:
                return columnName.equals(ColumnName.CASE_TYPE_ID)
                    || columnName.equals(ColumnName.USER_ROLE)
                    || columnName.equals(ColumnName.CRUD);
            case AUTHORISATION_CASE_FIELD:
                return columnName.equals(ColumnName.CASE_TYPE_ID)
                    || columnName.equals(ColumnName.CASE_FIELD_ID)
                    || columnName.equals(ColumnName.USER_ROLE)
                    || columnName.equals(ColumnName.CRUD);
            case AUTHORISATION_CASE_EVENT:
                return columnName.equals(ColumnName.CASE_TYPE_ID)
                    || columnName.equals(ColumnName.CASE_EVENT_ID)
                    || columnName.equals(ColumnName.USER_ROLE)
                    || columnName.equals(ColumnName.CRUD);
            case AUTHORISATION_CASE_STATE:
                return columnName.equals(ColumnName.CASE_TYPE_ID)
                    || columnName.equals(ColumnName.STATE_ID)
                    || columnName.equals(ColumnName.USER_ROLE)
                    || columnName.equals(ColumnName.CRUD);
            case SEARCH_ALIAS:
                return columnName.equals(ColumnName.CASE_TYPE_ID)
                    || columnName.equals(ColumnName.SEARCH_ALIAS_ID)
                    || columnName.equals(ColumnName.CASE_FIELD_ID);
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
