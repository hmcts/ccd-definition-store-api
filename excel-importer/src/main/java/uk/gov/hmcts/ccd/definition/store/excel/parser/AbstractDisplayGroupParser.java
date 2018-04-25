package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.field.FieldShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public abstract class AbstractDisplayGroupParser implements FieldShowConditionParser {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final ShowConditionParser showConditionParser;
    protected final ParseContext parseContext;
    protected final EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;
    protected DisplayGroupType displayGroupType;
    protected DisplayGroupPurpose displayGroupPurpose;
    protected ColumnName displayGroupLabel;
    protected ColumnName displayGroupId;
    protected ColumnName displayGroupOrder;
    protected ColumnName displayGroupFieldDisplayOrder;
    protected SheetName sheetName;
    protected Optional<ColumnName> groupShowConditionColumn = Optional.empty();
    protected Optional<ColumnName> fieldShowConditionColumn = Optional.empty();
    protected Optional<ColumnName> columnId = Optional.empty();
    protected boolean displayGroupItemMandatory;


    public AbstractDisplayGroupParser(ParseContext parseContext, ShowConditionParser showConditionParser, EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry) {
        this.parseContext = parseContext;
        this.showConditionParser = showConditionParser;
        this.entityToDefinitionDataItemRegistry = entityToDefinitionDataItemRegistry;
    }

    public ParseResult<DisplayGroupEntity> parseAll(Map<String, DefinitionSheet> definitionSheets) {
        logger.debug("Display group parsing...");

        final ParseResult<DisplayGroupEntity> result = new ParseResult<>();

        DefinitionSheet definitionSheet = definitionSheets.get(this.sheetName.getName());

        if (definitionSheet == null) {
            throw new MapperException(
                String.format("A definition must contain a %s sheet with at least one entry", this.sheetName.getName())
            );
        }

        final Map<String, List<DefinitionDataItem>> displayGroupsByCaseTypes = definitionSheets.get(this.sheetName.getName()).getDataItems().stream()
            .collect(groupingBy(dataItem -> dataItem.getString(ColumnName.CASE_TYPE_ID)));

        logger.debug("Display group parsing: {} case types detected", displayGroupsByCaseTypes.size());

        for (CaseTypeEntity caseType : parseContext.getCaseTypes()) {
            final String caseTypeId = caseType.getReference();
            final List<DefinitionDataItem> displayGroupItems = displayGroupsByCaseTypes.get(caseTypeId);

            // For a TAB at least one DG Item is required, for WIZARD, DG Item is not mandatory
            if (CollectionUtils.isEmpty(displayGroupItems) && this.displayGroupItemMandatory) {
                throw new MapperException(
                    String.format(
                        "At least one CaseField must be defined in the CaseTypeTab for case type %s",
                        caseTypeId));
            } else if (CollectionUtils.isEmpty(displayGroupItems)) {
                continue;
            }

            final Map<String, List<DefinitionDataItem>> groupDefinitions = displayGroupItems.stream().collect(groupingBy(dataItem -> {
                String caseEventId = dataItem.getString(ColumnName.CASE_EVENT_ID);
                return (caseEventId == null ? "" : caseEventId) + dataItem.getString(displayGroupId);
            }));

            logger.debug("Display group parsing: Case displayGroupType {}: {} groups detected", caseTypeId, groupDefinitions.size());

            for (Map.Entry<String, List<DefinitionDataItem>> groupDefinition : groupDefinitions.entrySet()) {
                final String groupId = groupDefinition.getKey();

                logger.debug(
                    "Display group parsing: Case displayGroupType {}: Group {}: {} fields detected",
                    caseTypeId,
                    groupId,
                    groupDefinition.getValue().size());

                ParseResult.Entry<DisplayGroupEntity> parseResult = parseGroup(caseType, groupId, groupDefinition);
                result.add(parseResult);
                entityToDefinitionDataItemRegistry.addDefinitionDataItemForEntity(parseResult.getValue(), groupDefinition.getValue().get(0));

                logger.info("Display group parsing: Case displayGroupType {}: Group {}: OK", caseTypeId, groupId);
            }

            logger.info("Display group parsing: Case displayGroupType {}: OK", caseTypeId);
        }

        logger.info("Display group parsing: OK");

        return result;
    }

    protected ParseResult.Entry<DisplayGroupEntity> parseGroup(CaseTypeEntity caseType, String groupId, Map.Entry<String, List<DefinitionDataItem>> groupDefinition) {
        final DefinitionDataItem sample = groupDefinition.getValue().get(0);

        final DisplayGroupEntity group = new DisplayGroupEntity();
        group.setCaseType(caseType);
        group.setReference(groupId);
        group.setChannel(sample.getString(ColumnName.CHANNEL));
        group.setLabel(sample.getString(this.displayGroupLabel));
        group.setOrder(sample.getInteger(this.displayGroupOrder));
        group.setType(this.displayGroupType);
        group.setPurpose(this.displayGroupPurpose);
        String eventId = sample.getString(ColumnName.CASE_EVENT_ID);
        if (eventId != null) { // eventId is only for Wizards and not mandatory
            group.setEvent(parseContext.getEventForCaseType(caseType.getReference(), eventId));
        }
        final List<DisplayGroupCaseFieldEntity> groupCaseFields = groupDefinition.getValue()
            .stream()
            .map(groupCaseFieldDefinition -> parseGroupCaseField(caseType, groupCaseFieldDefinition))
            .collect(toList());

        group.addDisplayGroupCaseFields(groupCaseFields);
        this.groupShowConditionColumn.ifPresent(column -> parseGroupShowCondition(column, group, groupDefinition.getValue()));
        return ParseResult.Entry.createNew(group);
    }

    private void parseGroupShowCondition(ColumnName column, DisplayGroupEntity group, List<DefinitionDataItem> groupDefinition) {
        if (groupDefinition.stream().filter(ddi -> ddi.getString(column) != null).count() > 1) {
            throw new MapperException(String.format("Please provide single condition in TabShowCondition column in CaseTypeTab for the tab %s", group.getReference()));
        }
        Optional<DefinitionDataItem> definitionDataItemOpt = groupDefinition.stream().filter(ddi -> ddi.getString(column) != null).findFirst();
        definitionDataItemOpt.ifPresent(ddi -> group.setShowCondition(parseShowCondition(ddi.getString(column))));
    }

    protected DisplayGroupCaseFieldEntity parseGroupCaseField(CaseTypeEntity caseType, DefinitionDataItem groupCaseFieldDefinition) {
        final String caseFieldId = groupCaseFieldDefinition.getString(ColumnName.CASE_FIELD_ID);

        final DisplayGroupCaseFieldEntity groupCaseField = new DisplayGroupCaseFieldEntity();
        groupCaseField.setCaseField(parseContext.getCaseFieldForCaseType(caseType.getReference(), caseFieldId));
        groupCaseField.setLiveFrom(groupCaseFieldDefinition.getLocalDate(ColumnName.LIVE_FROM));
        groupCaseField.setLiveTo(groupCaseFieldDefinition.getLocalDate(ColumnName.LIVE_TO));
        groupCaseField.setOrder(groupCaseFieldDefinition.getInteger(this.displayGroupFieldDisplayOrder));
        this.columnId.ifPresent(cId -> groupCaseField.setColumnNumber(groupCaseFieldDefinition.getInteger(cId)));
        this.fieldShowConditionColumn.ifPresent(sC -> groupCaseField.setShowCondition(groupCaseFieldDefinition.getString(sC)));

        entityToDefinitionDataItemRegistry.addDefinitionDataItemForEntity(groupCaseField, groupCaseFieldDefinition);

        return groupCaseField;
    }

    @Override
    public ShowConditionParser getShowConditionParser() {
        return showConditionParser;
    }
}
