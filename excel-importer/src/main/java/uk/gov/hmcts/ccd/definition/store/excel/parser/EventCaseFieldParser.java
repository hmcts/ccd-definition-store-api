package uk.gov.hmcts.ccd.definition.store.excel.parser;

import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.field.FieldShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DisplayContextColumn;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.validation.HiddenFieldsValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;

public class EventCaseFieldParser implements FieldShowConditionParser {

    private final ParseContext parseContext;

    private final ShowConditionParser showConditionParser;

    private final EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;
    private final HiddenFieldsValidator hiddenFieldsValidator;

    public EventCaseFieldParser(ParseContext parseContext,
                                ShowConditionParser showConditionParser,
                                EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry,
                                HiddenFieldsValidator hiddenFieldsValidator) {
        this.parseContext = parseContext;
        this.showConditionParser = showConditionParser;
        this.entityToDefinitionDataItemRegistry = entityToDefinitionDataItemRegistry;
        this.hiddenFieldsValidator = hiddenFieldsValidator;
    }

    public EventCaseFieldEntity parseEventCaseField(String caseTypeId, DefinitionDataItem eventCaseFieldDefinition) {

        final EventCaseFieldEntity eventCaseField = new EventCaseFieldEntity();

        final String caseFieldId = eventCaseFieldDefinition.getString(ColumnName.CASE_FIELD_ID);
        eventCaseField.setCaseField(parseContext.getCaseFieldForCaseType(caseTypeId, caseFieldId));
        DisplayContextColumn displayContextColumn = eventCaseFieldDefinition.getDisplayContext();
        eventCaseField.setDisplayContext(displayContextColumn.getDisplayContext());
        eventCaseField.setDisplayContextParameter(this.parseShowCondition(
            eventCaseFieldDefinition.getString(ColumnName.DISPLAY_CONTEXT_PARAMETER)));
        eventCaseField.setShowCondition(this.parseShowCondition(
            eventCaseFieldDefinition.getString(ColumnName.FIELD_SHOW_CONDITION)));
        eventCaseField.setShowSummaryChangeOption(
            eventCaseFieldDefinition.getBoolean(ColumnName.SHOW_SUMMARY_CHANGE_OPTION));
        eventCaseField.setShowSummaryContentOption(
            eventCaseFieldDefinition.getInteger(ColumnName.SHOW_SUMMARY_CONTENT_OPTION));
        eventCaseField.setLabel(eventCaseFieldDefinition.getString(ColumnName.CASE_EVENT_FIELD_LABEL));
        eventCaseField.setHintText(eventCaseFieldDefinition.getString(ColumnName.CASE_EVENT_FIELD_HINT));
        eventCaseField.setRetainHiddenValue(hiddenFieldsValidator.parseHiddenFields(eventCaseFieldDefinition));

        this.entityToDefinitionDataItemRegistry.addDefinitionDataItemForEntity(
            eventCaseField, eventCaseFieldDefinition);

        return eventCaseField;

    }

    @Override
    public ShowConditionParser getShowConditionParser() {
        return showConditionParser;
    }
}
