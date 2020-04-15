package uk.gov.hmcts.ccd.definition.store.excel.parser;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.CASE_FIELD_ID;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.CASE_TYPE_ID;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.LIST_ELEMENT_CODE;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.USER_ROLE;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.RESULTS_ORDERING;
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
import uk.gov.hmcts.ccd.definition.store.repository.entity.SortOrder;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

public abstract class GenericLayoutParser {
    private static final Logger logger = LoggerFactory.getLogger(GenericLayoutParser.class);

    public static final Pattern SORT_ORDER_PATTERN = Pattern.compile("^(1|2):(ASC|DESC)$");
    public static final String SORT_STRING_DELIMITER = ":";
    public static final String ALL_ROLES = "ALL_ROLES";

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

            validateSortOrders(layoutItems, caseType);

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
        final String sortOrder = definition.getString(RESULTS_ORDERING);
        if (StringUtils.isNotEmpty(sortOrder)) {
            populateSortOrder(layoutEntity, sortOrder);
        }
        entityToDefinitionDataItemRegistry.addDefinitionDataItemForEntity(layoutEntity, definition);
        return ParseResult.Entry.createNew(layoutEntity);
    }

    private UserRoleEntity getRoleEntity(GenericLayoutEntity layoutEntity, String sheetName, String userRole) {
        return parseContext.getIdamRole(userRole)
            .orElseThrow(() -> new MapperException(String.format("- Unknown idam role '%s' in worksheet '%s' for caseField '%s'",
                userRole, sheetName, layoutEntity.getCaseField().getReference())));
    }

    private void validateSortOrders(List<DefinitionDataItem> layoutItems, CaseTypeEntity caseType) {
        List<DefinitionDataItem> sortDataItems = layoutItems.stream()
            .filter(ddi -> StringUtils.isNotEmpty(ddi.getString(RESULTS_ORDERING)))
            .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(sortDataItems)) {
            String sheetName = layoutItems.get(0).getSheetName();
            // validate string pattern
            sortDataItems.forEach(item -> validateSortOrderPatternString(item, caseType.getReference()));
            // validate duplicate and missing gaps in the sort priorities
            validateDuplicateAndGaps(caseType, sheetName, sortDataItems);
        }
    }

    private void validateSortOrderPatternString(DefinitionDataItem dataItem, String caseTyeReference) {
        if (!SORT_ORDER_PATTERN.matcher(dataItem.getString(RESULTS_ORDERING)).matches()) {
            throw new MapperException(String.format("Invalid results ordering pattern '%s' in worksheet '%s' for caseType '%s' for caseField '%s'",
                dataItem.getString(RESULTS_ORDERING), dataItem.getSheetName(), caseTyeReference, dataItem.getString(CASE_FIELD_ID)));
        }
    }

    private void validateDuplicateAndGaps(CaseTypeEntity caseType, String sheetName, List<DefinitionDataItem> sortDataItems) {
        Map<String, List<Integer>> sortPrioritiesByUserRole = getSortPrioritiesByRole(sortDataItems);
        sortPrioritiesByUserRole.values().forEach(items -> {
            checkDuplicateSortOrders(items, sheetName, caseType.getReference());
            checkGapsInSortPriority(items, sheetName, caseType.getReference());
        });
    }

    private void checkGapsInSortPriority(List<Integer> items, String sheetName, String caseType) {
        boolean noGaps = items.stream().allMatch(e -> items.containsAll(IntStream.range(1, e).boxed().collect(Collectors.toList())));
        if (!noGaps) {
            throw new MapperException(String.format("Missing sort order priority in worksheet '%s' for caseType '%s'", sheetName, caseType));
        }
    }

    private void checkDuplicateSortOrders(List<Integer> items, String sheetName, String caseType) {
        boolean isUnique = items.stream().allMatch(new HashSet<>()::add);
        if (!isUnique) {
            throw new MapperException(String.format("Duplicate sort order priority in worksheet '%s' for caseType '%s'", sheetName, caseType));
        }
    }

    private Map<String, List<Integer>> getSortPrioritiesByRole(List<DefinitionDataItem> sortDataItems) {
        Map<String, List<Integer>> sortPrioritiesByUserRole = new HashMap<>();

        sortDataItems.stream().forEach(ddi -> {
            String key = StringUtils.isNotEmpty(ddi.getString(USER_ROLE)) ? ddi.getString(USER_ROLE) : ALL_ROLES;
            List<Integer> priorities = sortPrioritiesByUserRole.getOrDefault(key, new ArrayList<>());
            priorities.add(Integer.valueOf(ddi.getString(RESULTS_ORDERING).split(SORT_STRING_DELIMITER)[0]));
            sortPrioritiesByUserRole.put(key, priorities);
        });
        sortPrioritiesByUserRole.keySet().forEach(key -> {
            if (!key.equalsIgnoreCase(ALL_ROLES) && sortPrioritiesByUserRole.get(ALL_ROLES) != null) {
                sortPrioritiesByUserRole.get(key).addAll(sortPrioritiesByUserRole.get(ALL_ROLES));
            }
        });
        return sortPrioritiesByUserRole;
    }

    protected static SortOrder getSortOrder(String sortOrderString) {
        String[] tokens = sortOrderString.split(SORT_STRING_DELIMITER);
        return new SortOrder(Integer.valueOf(tokens[0]), tokens[1]);
    }

    protected Logger getLogger() {
        return logger;
    }

    protected abstract void populateSortOrder(GenericLayoutEntity layoutEntity, String sortOrde);

    protected abstract DefinitionSheet getDefinitionSheet(Map<String, DefinitionSheet> definitionSheets);

    protected abstract String getLayoutName();

    protected abstract GenericLayoutEntity createLayoutCaseFieldEntity();
}
