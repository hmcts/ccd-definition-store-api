package uk.gov.hmcts.ccd.definition.store.excel.parser;

import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.field.FieldShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DisplayContextColumn;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventComplexTypeEntity;

import java.util.ArrayList;
import java.util.List;

public class EventCaseFieldComplexTypeParser implements FieldShowConditionParser {

    private final ShowConditionParser showConditionParser;

    public EventCaseFieldComplexTypeParser(ShowConditionParser showConditionParser) {
        this.showConditionParser = showConditionParser;
    }

    public List<EventComplexTypeEntity> parseEventCaseFieldComplexType(List<DefinitionDataItem> dataItems) {

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
            eventComplexTypeEntities.add(eventComplexTypeEntity);
        }
        return eventComplexTypeEntities;
    }

    @Override
    public ShowConditionParser getShowConditionParser() {
        return showConditionParser;
    }
}
