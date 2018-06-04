package uk.gov.hmcts.ccd.definition.store.excel.parser;

import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.field.FieldShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DisplayContextColumn;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;

public class EventCaseFieldParser implements FieldShowConditionParser {

    private final ParseContext parseContext;

    private final ShowConditionParser showConditionParser;

    private final EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;

    public EventCaseFieldParser(ParseContext parseContext,
                                ShowConditionParser showConditionParser,
                                EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry) {
        this.parseContext = parseContext;
        this.showConditionParser = showConditionParser;
        this.entityToDefinitionDataItemRegistry = entityToDefinitionDataItemRegistry;
    }

    public EventCaseFieldEntity parseEventCaseField(String caseTypeId, DefinitionDataItem eventCaseFieldDefinition) {

        final EventCaseFieldEntity eventCaseField = new EventCaseFieldEntity();

        final String caseFieldId = eventCaseFieldDefinition.getString(ColumnName.CASE_FIELD_ID);
        eventCaseField.setCaseField(parseContext.getCaseFieldForCaseType(caseTypeId, caseFieldId));
        DisplayContextColumn displayContextColumn = eventCaseFieldDefinition.getDisplayContext();
        eventCaseField.setDisplayContext(displayContextColumn.getDisplayContext());
        eventCaseField.setShowCondition(this.parseShowCondition(eventCaseFieldDefinition.getString(ColumnName.FIELD_SHOW_CONDITION)));

        eventCaseField.setShowSummaryChangeOption(eventCaseFieldDefinition.getBoolean(ColumnName.SHOW_SUMMARY_CHANGE_OPTION));
        eventCaseField.setShowSummaryContentOption(eventCaseFieldDefinition.getInteger(ColumnName.SHOW_SUMMARY_CONTENT_OPTION));
        this.entityToDefinitionDataItemRegistry.addDefinitionDataItemForEntity(eventCaseField, eventCaseFieldDefinition);

        return eventCaseField;

    }

    @Override
    public ShowConditionParser getShowConditionParser() {
        return showConditionParser;
    }
}
