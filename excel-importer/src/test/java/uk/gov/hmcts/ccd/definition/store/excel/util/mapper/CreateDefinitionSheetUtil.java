package uk.gov.hmcts.ccd.definition.store.excel.util.mapper;

import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

import uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model.DefinitionDataItem;

/**
 * Util class to assist with creating mock Case Definitions in tests.
 */
class CreateDefinitionSheetUtil {
    private CreateDefinitionSheetUtil() {
    }

    /**
     * Create a DefinitionDataItem for the given Map of columns and their attribute.
     *
     * @param attributes Map of column values for each Column
     * @return the created DefinitionDataItem
     */
    private static DefinitionDataItem createDefinitionDataItem(Map<ColumnName, Object> attributes) {
        DefinitionDataItem item = new DefinitionDataItem();
        for (ColumnName column : attributes.keySet()) {
            item.addAttribute(column.toString(), attributes.get(column));
        }
        return item;
    }

    /**
     * Create a Case Field.
     *
     * @param id            - case field id
     * @param caseTypeId    - linked case type id
     * @param shortDesc     - case type description
     * @param hintText      - case field hint text
     * @param fieldType     - case field type
     * @param hidden        - boolean to indicate if case field is hidden
     * @param regEx         - regular expression for case field
     * @param securityLabel - security label for case field
     * @param fixedListCode - fixed list item code
     * @return the created Case Field
     */
    static DefinitionDataItem createCaseFieldItem(String id, String caseTypeId, String shortDesc, String hintText,
                                                  String fieldType, String hidden, String regEx, String securityLabel,
                                                  String min, String max, String fixedListCode) {
        Map<ColumnName, Object> attributes = new EnumMap<>(ColumnName.class);
        attributes.put(ColumnName.ID, id);
        attributes.put(ColumnName.CASE_TYPE_ID, caseTypeId);
        attributes.put(ColumnName.NAME, shortDesc);
        attributes.put(ColumnName.HINT_TEXT, hintText);
        attributes.put(ColumnName.FIELD_TYPE, fieldType);
        attributes.put(ColumnName.DEFAULT_HIDDEN, hidden);
        attributes.put(ColumnName.REGULAR_EXPRESSION, regEx);
        attributes.put(ColumnName.SECURITY_CLASSIFICATION, securityLabel);
        attributes.put(ColumnName.MIN, min);
        attributes.put(ColumnName.MAX, max);
        attributes.put(ColumnName.FIELD_TYPE_PARAMETER, fixedListCode);
        return createDefinitionDataItem(attributes);
    }

    /**
     * Create a Case State.
     *
     * @param id         - case state id
     * @param shortDesc  - case state description
     * @param caseTypeId - linked case type id
     * @return the created Case State
     */
    static DefinitionDataItem createStateItem(String id, String shortDesc, String caseTypeId) {
        Map<ColumnName, Object> attributes = new EnumMap<>(ColumnName.class);
        attributes.put(ColumnName.ID, id);
        attributes.put(ColumnName.NAME, shortDesc);
        attributes.put(ColumnName.CASE_TYPE_ID, caseTypeId);
        return createDefinitionDataItem(attributes);
    }

    /**
     * Create a Case Event.
     *
     * @param id        - case event id
     * @param name      - case event name
     * @param desc      - case event description
     * @param preState  - valid case event pre-states
     * @param postState - case event post-state
     * @return the created CaseEvent
     */
    static DefinitionDataItem createCaseEventItem(String id, String name, String desc, String preState, String postState, String caseTypeId) {
        return createCaseEventItem(id, name, desc, preState, postState, caseTypeId, null, null, null, null, null, null);
    }

    /**
     * Creates a Case Event.
     *
     * @param id                                    - case event id
     * @param name                                  - case event name
     * @param desc                                  - case event description
     * @param preState                              - pre state the case event should expect
     * @param postState                             - the state in which the case event should transition to
     * @param caseTypeId                            - case type id associated to the case event
     * @param callBackURLAboutToStartEvent          - call back url for about to start
     * @param retriesTimeoutAboutToStartEvent       - number of retries to attempt
     * @param callBackURLAboutToSubmitEvent         - call back url for about to submit
     * @param retriesTimeoutURLAboutToSubmitEvent   - number of retries to attempt
     * @param callBackURLSubmittedEvent             - call back url for submitted
     * @param retriesTimeoutURLSubmittedEvent       - number of retries to attempt
     * @return the created CaseEvent
     */
    static DefinitionDataItem createCaseEventItem(final String id,
                                                  final String name,
                                                  final String desc,
                                                  final String preState,
                                                  final String postState,
                                                  final String caseTypeId,
                                                  final String callBackURLAboutToStartEvent,
                                                  final String retriesTimeoutAboutToStartEvent,
                                                  final String callBackURLAboutToSubmitEvent,
                                                  final String retriesTimeoutURLAboutToSubmitEvent,
                                                  final String callBackURLSubmittedEvent,
                                                  final String retriesTimeoutURLSubmittedEvent) {
        final Map<ColumnName, Object> attributes = new EnumMap<>(ColumnName.class);
        attributes.put(ColumnName.ID, id);
        attributes.put(ColumnName.NAME, name);
        attributes.put(ColumnName.DESCRIPTION, desc);
        attributes.put(ColumnName.PRE_CONDITION_STATE, preState);
        attributes.put(ColumnName.POST_CONDITION_STATE, postState);
        attributes.put(ColumnName.CASE_TYPE_ID, caseTypeId);
        attributes.put(ColumnName.CALLBACK_URL_ABOUT_TO_START_EVENT, callBackURLAboutToStartEvent);
        attributes.put(ColumnName.CALLBACK_URL_ABOUT_TO_SUBMIT_EVENT, callBackURLAboutToSubmitEvent);
        attributes.put(ColumnName.CALLBACK_URL_SUBMITTED_EVENT, callBackURLSubmittedEvent);
        attributes.put(ColumnName.RETRIES_TIMEOUT_ABOUT_TO_START_EVENT, retriesTimeoutAboutToStartEvent);
        attributes.put(ColumnName.RETRIES_TIMEOUT_URL_ABOUT_TO_SUBMIT_EVENT, retriesTimeoutURLAboutToSubmitEvent);
        attributes.put(ColumnName.RETRIES_TIMEOUT_URL_SUBMITTED_EVENT, retriesTimeoutURLSubmittedEvent);
        return createDefinitionDataItem(attributes);
    }

    /**
     * Create a Work Basket Result Field.
     *
     * @param caseTypeId   - linked case type id
     * @param caseFieldId  - linked case field id
     * @param label        - label for result field
     * @param displayOrder - display order of result field
     * @return the created Work Basket Result Field
     */
    static DefinitionDataItem createWorkBasketResultFieldItem(String caseTypeId, String caseFieldId, String label, double displayOrder) {
        Map<ColumnName, Object> attributes = new EnumMap<>(ColumnName.class);
        attributes.put(ColumnName.CASE_TYPE_ID, caseTypeId);
        attributes.put(ColumnName.CASE_FIELD_ID, caseFieldId);
        attributes.put(ColumnName.LABEL, label);
        attributes.put(ColumnName.DISPLAY_ORDER, displayOrder);
        return createDefinitionDataItem(attributes);
    }

    /**
     * Create a Case Tab Collection.
     *
     * @param liveFrom             - live from date
     * @param liveTo               - live to date
     * @param caseTypeId           - linked case type id
     * @param channel              - linked channel
     * @param tabId                - tab id
     * @param tabLabel             - tab label
     * @param tabDisplayOrder      - tab display order
     * @param caseFieldId          - linked case field id
     * @param tabFieldDisplayOrder - tab field display order
     * @return the created Case Tab Collection
     */
    static DefinitionDataItem createCaseTabCollectionItem(Date liveFrom, Date liveTo, String caseTypeId, String channel, String tabId,
                                                          String tabLabel, double tabDisplayOrder, String caseFieldId, double tabFieldDisplayOrder) {
        Map<ColumnName, Object> attributes = new EnumMap<>(ColumnName.class);
        attributes.put(ColumnName.LIVE_FROM, liveFrom);
        attributes.put(ColumnName.LIVE_TO, liveTo);
        attributes.put(ColumnName.CASE_TYPE_ID, caseTypeId);
        attributes.put(ColumnName.CHANNEL, channel);
        attributes.put(ColumnName.TAB_ID, tabId);
        attributes.put(ColumnName.TAB_LABEL, tabLabel);
        attributes.put(ColumnName.TAB_DISPLAY_ORDER, tabDisplayOrder);
        attributes.put(ColumnName.CASE_FIELD_ID, caseFieldId);
        attributes.put(ColumnName.TAB_FIELD_DISPLAY_ORDER, tabFieldDisplayOrder);
        return createDefinitionDataItem(attributes);
    }

    static DefinitionDataItem createSearchFieldItem(Date liveFrom, Date liveTo, String caseTypeId, String caseFieldId, String label, double displayOrder) {
        Map<ColumnName, Object> attributes = new EnumMap<>(ColumnName.class);
        attributes.put(ColumnName.LIVE_FROM, liveFrom);
        attributes.put(ColumnName.LIVE_TO, liveTo);
        attributes.put(ColumnName.CASE_TYPE_ID, caseTypeId);
        attributes.put(ColumnName.CASE_FIELD_ID, caseFieldId);
        attributes.put(ColumnName.LABEL, label);
        attributes.put(ColumnName.DISPLAY_ORDER, displayOrder);
        return createDefinitionDataItem(attributes);
    }

    static DefinitionDataItem createComplexTypeItem(Date liveFrom,
                                                    Date liveTo,
                                                    String id,
                                                    String listElementCode,
                                                    String fieldType,
                                                    String label,
                                                    String regularExpression,
                                                    String hintText,
                                                    String fixListCode,
                                                    String defaultHidden,
                                                    String securityLabel,
                                                    Object min,
                                                    Object max) {
        Map<ColumnName, Object> attributes = new EnumMap<>(ColumnName.class);
        attributes.put(ColumnName.LIVE_FROM, liveFrom);
        attributes.put(ColumnName.LIVE_TO, liveTo);
        attributes.put(ColumnName.ID, id);
        attributes.put(ColumnName.LIST_ELEMENT_CODE, listElementCode);
        attributes.put(ColumnName.FIELD_TYPE, fieldType);
        attributes.put(ColumnName.LABEL, label);
        attributes.put(ColumnName.REGULAR_EXPRESSION, regularExpression);
        attributes.put(ColumnName.HINT_TEXT, hintText);
        attributes.put(ColumnName.FIELD_TYPE_PARAMETER, fixListCode);
        attributes.put(ColumnName.DEFAULT_HIDDEN, defaultHidden);
        attributes.put(ColumnName.SECURITY_CLASSIFICATION, securityLabel);
        attributes.put(ColumnName.MIN, min);
        attributes.put(ColumnName.MAX, max);
        return createDefinitionDataItem(attributes);
    }

    static DefinitionDataItem createFixListCodeItem(Date liveFrom, Date liveTo, String id, String listElementCode, String listElement) {
        Map<ColumnName, Object> attributes = new EnumMap<>(ColumnName.class);
        attributes.put(ColumnName.LIVE_FROM, liveFrom);
        attributes.put(ColumnName.LIVE_TO, liveTo);
        attributes.put(ColumnName.ID, id);
        attributes.put(ColumnName.LIST_ELEMENT_CODE, listElementCode);
        attributes.put(ColumnName.LIST_ELEMENT, listElement);
        return createDefinitionDataItem(attributes);
    }
}
