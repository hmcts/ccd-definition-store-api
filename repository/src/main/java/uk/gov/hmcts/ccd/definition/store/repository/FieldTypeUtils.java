package uk.gov.hmcts.ccd.definition.store.repository;

public class FieldTypeUtils {

    public static final String BASE_COLLECTION = "Collection";
    public static final String BASE_COMPLEX = "Complex";
    public static final String BASE_FIXED_LIST = "FixedList";
    public static final String BASE_RADIO_FIXED_LIST = "FixedRadioList";
    public static final String BASE_MULTI_SELECT_LIST = "MultiSelectList";
    public static final String BASE_TEXT = "Text";
    public static final String BASE_NUMBER = "Number";
    public static final String BASE_EMAIL = "Email";
    public static final String BASE_YES_OR_NO = "YesOrNo";
    public static final String BASE_DATE = "Date";
    public static final String BASE_DATE_TIME = "DateTime";
    public static final String BASE_POST_CODE = "Postcode";
    public static final String BASE_MONEY_GBP = "MoneyGBP";
    public static final String BASE_PHONE_UK = "PhoneUK";
    public static final String BASE_TEXT_AREA = "TextArea";
    public static final String BASE_DOCUMENT = "Document";
    public static final String BASE_LABEL = "Label";
    public static final String BASE_CASE_PAYMENT_HISTORY_VIEWER = "CasePaymentHistoryViewer";

    public static Boolean isList(String baseTypeReference) {
        return BASE_FIXED_LIST.equals(baseTypeReference) || BASE_MULTI_SELECT_LIST.equals(baseTypeReference) || BASE_RADIO_FIXED_LIST.equals(baseTypeReference);
    }

}
