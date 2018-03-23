package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.WORK_BASKET_INPUT_FIELD;

public abstract class GenericLayoutParser {
    private static final Logger logger = LoggerFactory.getLogger(GenericLayoutParser.class);

    private final EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;
    protected final ParseContext parseContext;

    public GenericLayoutParser(final ParseContext parseContext,
                               final EntityToDefinitionDataItemRegistry registry) {
        this.parseContext = parseContext;
        this.entityToDefinitionDataItemRegistry = registry;
    }

    public ParseResult<GenericLayoutEntity> parseAll(Map<String, DefinitionSheet> definitionSheets) {
        getLogger().debug("Layout parsing...");

        final ParseResult<GenericLayoutEntity> result = new ParseResult<>();

        final Map<String, List<DefinitionDataItem>> layoutItemsByCaseTypes = getDefinitionSheet(definitionSheets)
            .groupDataItemsByCaseType();
        final List<DefinitionDataItem> unknownDefinition = getUnknownDataDefinitionItems(definitionSheets);
        if (null != unknownDefinition && !unknownDefinition.isEmpty()) {
            List<String> message = unknownDefinition.stream()
                .map(definitionDataItem -> String.format("Unknown Case Type %s for layout %s",
                                                         definitionDataItem.findAttribute(ColumnName.CASE_TYPE_ID),
                                                         getLayoutName()))
                .collect(Collectors.toList());
            throw new MapperException(message.stream().collect(Collectors.joining(",")));
        }

        getLogger().debug("Layout parsing: {} case types detected", layoutItemsByCaseTypes.size());

        for (CaseTypeEntity caseType : parseContext.getCaseTypes()) {
            final String caseTypeId = caseType.getReference();
            final List<DefinitionDataItem> layoutItems = layoutItemsByCaseTypes.get(caseTypeId);

            if (CollectionUtils.isEmpty(layoutItems) && !WORK_BASKET_INPUT_FIELD.getName()
                .equalsIgnoreCase(this.getLayoutName())) {
                throw new SpreadsheetParsingException(String.format(
                    "At least one layout case field must be defined for case type %s and layout %s",
                    caseTypeId,
                    getLayoutName()));
            } else {
                addParseLayoutCaseField(result, caseType, caseTypeId, layoutItems);
            }
        }

        getLogger().info("Layout parsing: OK");

        return result;
    }

    private void addParseLayoutCaseField(final ParseResult<GenericLayoutEntity> result,
                                         final CaseTypeEntity caseType,
                                         final String caseTypeId,
                                         final List<DefinitionDataItem> layoutItems) {
        if (null != layoutItems) {
            getLogger().debug("Layout parsing: Case type {}: {} fields detected",
                              caseTypeId,
                              layoutItems.size());

            for (DefinitionDataItem layoutCaseFieldDefinition : layoutItems) {
                result.add(parseLayoutCaseField(caseType, layoutCaseFieldDefinition));
            }

            getLogger().info("Layout parsing: Case type {}: OK", caseTypeId, layoutItems.size());
        }
    }

    private List<DefinitionDataItem> getUnknownDataDefinitionItems(Map<String, DefinitionSheet> definitionSheets) {
        return getDefinitionSheet(definitionSheets).getDataItems()
            .stream()
            .filter(definitionDataItem -> parseContext.getCaseTypes()
                .stream()
                .noneMatch(caseTypeEntity -> caseTypeEntity.getReference()
                    .equalsIgnoreCase(definitionDataItem.getString(ColumnName.CASE_TYPE_ID))))
            .collect(Collectors.toList());
    }

    private ParseResult.Entry<GenericLayoutEntity> parseLayoutCaseField(CaseTypeEntity caseType,
                                                                        DefinitionDataItem definition) {
        final String caseFieldId = definition.getString(ColumnName.CASE_FIELD_ID);
        final GenericLayoutEntity layoutCaseField = createLayoutCaseFieldEntity();
        layoutCaseField.setCaseType(caseType);
        layoutCaseField.setCaseField(parseContext.getCaseFieldForCaseType(caseType.getReference(), caseFieldId));
        layoutCaseField.setLiveFrom(definition.getLocalDate(ColumnName.LIVE_FROM));
        layoutCaseField.setLiveTo(definition.getLocalDate(ColumnName.LIVE_TO));
        layoutCaseField.setLabel(definition.getString(ColumnName.LABEL));
        layoutCaseField.setOrder(definition.getInteger(ColumnName.DISPLAY_ORDER));

        entityToDefinitionDataItemRegistry.addDefinitionDataItemForEntity(layoutCaseField, definition);
        return ParseResult.Entry.createNew(layoutCaseField);
    }

    protected Logger getLogger() {
        return logger;
    }

    protected abstract DefinitionSheet getDefinitionSheet(Map<String, DefinitionSheet> definitionSheets);

    protected abstract String getLayoutName();

    protected abstract GenericLayoutEntity createLayoutCaseFieldEntity();
}
