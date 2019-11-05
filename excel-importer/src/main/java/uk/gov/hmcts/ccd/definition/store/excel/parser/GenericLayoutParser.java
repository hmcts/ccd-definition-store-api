package uk.gov.hmcts.ccd.definition.store.excel.parser;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.CASE_FIELD_ID;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.CASE_TYPE_ID;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.LIST_ELEMENT_CODE;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.USER_ROLE;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.WORK_BASKET_INPUT_FIELD;

import liquibase.util.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

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
                .map(definitionDataItem -> String.format("Unknown Case Type '%s' for layout '%s'",
                    definitionDataItem.findAttribute(ColumnName.CASE_TYPE_ID), getLayoutName()))
                .collect(Collectors.toList());
            throw new MapperException(message.stream().collect(Collectors.joining(",")));
        }

        getLogger().debug("Layout parsing: {} case types detected", layoutItemsByCaseTypes.size());

        for (CaseTypeEntity caseType : parseContext.getCaseTypes()) {
            final String caseTypeId = caseType.getReference();
            final List<DefinitionDataItem> layoutItems = layoutItemsByCaseTypes.get(caseTypeId);

            if (CollectionUtils.isEmpty(layoutItems) && !WORK_BASKET_INPUT_FIELD.getName()
                .equalsIgnoreCase(this.getLayoutName())) {
                throw new MapperException(String.format(
                    "At least one layout case field must be defined for case type %s and layout %s",
                    caseTypeId, getLayoutName()));
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
            getLogger().debug("Layout parsing: Case type {}: {} fields detected", caseTypeId, layoutItems.size());

            if (hasDuplicateRows(layoutItems)) {
                throw new MapperException(
                    String.format("Please make sure each row in worksheet %s is unique for case type %s",
                        layoutItems.get(0).getSheetName(), caseType.getReference()));
            }

            for (DefinitionDataItem layoutCaseFieldDefinition : layoutItems) {
                result.add(parseLayoutCaseField(caseType, layoutCaseFieldDefinition));
            }

            getLogger().info("Layout parsing: Case type {}: OK", caseTypeId, layoutItems.size());
        }
    }

    private boolean hasDuplicateRows(List<DefinitionDataItem> layoutItems) {
        return layoutItems
            .stream()
            .anyMatch(ddi ->
                layoutItems.stream().filter(item -> (StringUtils.isNotEmpty(ddi.getString(USER_ROLE)) ? ddi.getString(USER_ROLE).equalsIgnoreCase(item.getString(USER_ROLE)) : StringUtils.isEmpty(item.getString(USER_ROLE)))
                    && ddi.getString(CASE_TYPE_ID).equalsIgnoreCase(item.getString(CASE_TYPE_ID))
                    && ddi.getString(CASE_FIELD_ID).equalsIgnoreCase(item.getString(CASE_FIELD_ID))
                    && (StringUtils.isNotEmpty(ddi.getString(LIST_ELEMENT_CODE)) ? ddi.getString(LIST_ELEMENT_CODE).equalsIgnoreCase(item.getString(LIST_ELEMENT_CODE)) : StringUtils.isEmpty(item.getString(LIST_ELEMENT_CODE))))
                    .count() > 1);
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
        final GenericLayoutEntity layoutEntity = createLayoutCaseFieldEntity();
        layoutEntity.setCaseType(caseType);
        layoutEntity.setCaseField(parseContext.getCaseFieldForCaseType(caseType.getReference(), caseFieldId));
        layoutEntity.setLiveFrom(definition.getLocalDate(ColumnName.LIVE_FROM));
        layoutEntity.setLiveTo(definition.getLocalDate(ColumnName.LIVE_TO));
        layoutEntity.setCaseFieldElementPath(definition.getString(LIST_ELEMENT_CODE));
        layoutEntity.setLabel(definition.getString(ColumnName.LABEL));
        layoutEntity.setOrder(definition.getInteger(ColumnName.DISPLAY_ORDER));
        final String userRole = definition.getString(USER_ROLE);
        if (StringUtils.isNotEmpty(userRole)) {
            layoutEntity.setUserRole(getRoleEntity(layoutEntity, definition.getSheetName(), userRole));
        }
        entityToDefinitionDataItemRegistry.addDefinitionDataItemForEntity(layoutEntity, definition);
        return ParseResult.Entry.createNew(layoutEntity);
    }

    private UserRoleEntity getRoleEntity(GenericLayoutEntity layoutEntity, String sheetName, String userRole) {
        return parseContext.getIdamRole(userRole)
            .orElseThrow(() -> new MapperException(String.format("- Unknown idam role '%s' in worksheet '%s' for caseField '%s'",
                userRole, sheetName, layoutEntity.getCaseField().getReference())));
    }

    protected Logger getLogger() {
        return logger;
    }

    protected abstract DefinitionSheet getDefinitionSheet(Map<String, DefinitionSheet> definitionSheets);

    protected abstract String getLayoutName();

    protected abstract GenericLayoutEntity createLayoutCaseFieldEntity();
}
