package uk.gov.hmcts.ccd.definition.store.excel.parser;

import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.field.FieldShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DisplayContextColumn;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.validation.HiddenFieldsValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventComplexTypeEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventCaseFieldComplexTypeParser implements FieldShowConditionParser {

    private final ShowConditionParser showConditionParser;
    private final HiddenFieldsValidator hiddenFieldsValidator;

    public EventCaseFieldComplexTypeParser(ShowConditionParser showConditionParser,
                                           HiddenFieldsValidator hiddenFieldsValidator) {
        this.showConditionParser = showConditionParser;
        this.hiddenFieldsValidator = hiddenFieldsValidator;
    }

    public List<EventComplexTypeEntity> parseEventCaseFieldComplexType(List<DefinitionDataItem> dataItems,
                                                                       Map<String, DefinitionSheet> definitionSheets) {

        List<EventComplexTypeEntity> eventComplexTypeEntities = new ArrayList<>();
        for (DefinitionDataItem definitionDataItem : dataItems) {
            EventComplexTypeEntity eventComplexTypeEntity = new EventComplexTypeEntity();

            eventComplexTypeEntity.setReference(definitionDataItem.getString(ColumnName.LIST_ELEMENT_CODE));
            eventComplexTypeEntity.setLabel(definitionDataItem.getString(ColumnName.EVENT_ELEMENT_LABEL));
            eventComplexTypeEntity.setHint(definitionDataItem.getString(ColumnName.EVENT_HINT_TEXT));
            eventComplexTypeEntity.setLiveFrom(definitionDataItem.getLocalDate(ColumnName.LIVE_FROM));
            eventComplexTypeEntity.setLiveTo(definitionDataItem.getLocalDate(ColumnName.LIVE_TO));
            eventComplexTypeEntity.setOrder(definitionDataItem.getInteger(ColumnName.FIELD_DISPLAY_ORDER));

            eventComplexTypeEntity.setDefaultValue(definitionDataItem.getString(ColumnName.DEFAULT_VALUE));
            DisplayContextColumn displayContextColumn = definitionDataItem.getDisplayContext();
            eventComplexTypeEntity.setDisplayContext(displayContextColumn.getDisplayContext());
            eventComplexTypeEntity.setShowCondition(parseShowCondition(
                definitionDataItem.getString(ColumnName.FIELD_SHOW_CONDITION)));

            eventComplexTypeEntity.setPublish(definitionDataItem.getBooleanOrDefault(ColumnName.PUBLISH, false));
            eventComplexTypeEntity.setPublishAs(definitionDataItem.getString(ColumnName.PUBLISH_AS));

            eventComplexTypeEntities.add(eventComplexTypeEntity);
            eventComplexTypeEntity.setRetainHiddenValue(hiddenFieldsValidator
                .parseCaseEventComplexTypesHiddenFields(definitionDataItem, definitionSheets));
        }
        return eventComplexTypeEntities;
    }

    @Override
    public ShowConditionParser getShowConditionParser() {
        return showConditionParser;
    }
}
