package uk.gov.hmcts.ccd.definition.store.excel.parser;

import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.field.FieldShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessProfileEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SortOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.ACCESS_PROFILE;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.CASE_FIELD_ID;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.CASE_TYPE_ID;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.FIELD_SHOW_CONDITION;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.LIST_ELEMENT_CODE;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.RESULTS_ORDERING;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.USE_CASE;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.WORK_BASKET_INPUT_FIELD;

public abstract class GenericLayoutParser implements FieldShowConditionParser {
    private static final Logger logger = LoggerFactory.getLogger(GenericLayoutParser.class);

    private static final Pattern SORT_ORDER_PATTERN = Pattern.compile("^(1|2):(ASC|DESC)$");
    private static final String SORT_STRING_DELIMITER = ":";
    private static final String ALL_ACCESS_PROFILES = "ALL_ACCESS_PROFILES";

    private final EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;
    protected final ParseContext parseContext;
    private final ShowConditionParser showConditionParser;

    public GenericLayoutParser(final ParseContext parseContext,
                               final EntityToDefinitionDataItemRegistry registry,
                               final ShowConditionParser showConditionParser) {
        this.parseContext = parseContext;
        this.entityToDefinitionDataItemRegistry = registry;
        this.showConditionParser = showConditionParser;
    }

    public ParseResult<GenericLayoutEntity> parseAll(Map<String, DefinitionSheet> definitionSheets) {
        getLogger().debug("Layout parsing...");
        final ParseResult<GenericLayoutEntity> result = new ParseResult<>();
        final Map<String, List<DefinitionDataItem>> layoutItemsByCaseTypes = getDefinitionSheet(definitionSheets)
            .groupDataItemsByCaseType();
        checkForUnknownDefinitions(definitionSheets);
        getLogger().debug("Layout parsing: {} case types detected", layoutItemsByCaseTypes.size());

        for (CaseTypeEntity caseType : parseContext.getCaseTypes()) {
            final String caseTypeId = caseType.getReference();
            final List<DefinitionDataItem> layoutItems = layoutItemsByCaseTypes.get(caseTypeId);

            if ((CollectionUtils.isEmpty(layoutItems)
                && !WORK_BASKET_INPUT_FIELD.getName().equalsIgnoreCase(this.getLayoutName()))) {
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

    private void checkForUnknownDefinitions(Map<String, DefinitionSheet> definitionSheets) {
        final List<DefinitionDataItem> unknownDefinition = getUnknownDataDefinitionItems(definitionSheets);
        if (null != unknownDefinition && !unknownDefinition.isEmpty()) {
            List<String> message = unknownDefinition.stream()
                .map(definitionDataItem -> String.format("Unknown Case Type '%s' for layout '%s'",
                    definitionDataItem.findAttribute(ColumnName.CASE_TYPE_ID), getLayoutName()))
                .collect(Collectors.toList());
            throw new MapperException(message.stream().collect(Collectors.joining(",")));
        }
    }

    public ParseResult<GenericLayoutEntity> parseAllForSearchCases(Map<String, DefinitionSheet> definitionSheets) {

        getLogger().debug("Layout parsing...");
        final ParseResult<GenericLayoutEntity> result = new ParseResult<>();
        final Map<String, List<DefinitionDataItem>> layoutItemsByCaseTypes = getDefinitionSheet(definitionSheets)
            .groupDataItemsByCaseType();
        checkForUnknownDefinitions(definitionSheets);
        getLogger().debug("Layout parsing: {} case types detected", layoutItemsByCaseTypes.size());

        for (CaseTypeEntity caseType : parseContext.getCaseTypes()) {
            final String caseTypeId = caseType.getReference();
            final List<DefinitionDataItem> layoutItems = layoutItemsByCaseTypes.get(caseTypeId);

            addParseLayoutCaseField(result, caseType, caseTypeId, layoutItems);
        }

        getLogger().info("Layout parsing: OK");

        return result;
    }

    @SuppressWarnings("checkstyle:EmptyBlock")
    public ParseResult<GenericLayoutEntity> parseAllSearchCases(Map<String, DefinitionSheet> definitionSheets) {
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

            getLogger().info("Layout parsing: Case type {}: OK", caseTypeId);
        }
    }

    private boolean hasDuplicateRows(List<DefinitionDataItem> layoutItems) {
        return layoutItems
            .stream()
            .anyMatch(ddi -> {
                if (ddi.getSheetName().equals(SheetName.SEARCH_CASES_RESULT_FIELDS.getName())) {
                    return hasDuplicateRowsForSearchCases(layoutItems, ddi);
                } else {
                    return hasDuplicateRows(layoutItems, ddi);
                }
            });

    }

    private boolean hasDuplicateRows(List<DefinitionDataItem> layoutItems, DefinitionDataItem ddi) {
        return layoutItems.stream().filter(item -> (StringUtils.isNotEmpty(ddi.getString(ACCESS_PROFILE))
            ? ddi.getString(ACCESS_PROFILE).equalsIgnoreCase(item.getString(ACCESS_PROFILE))
            : StringUtils.isEmpty(item.getString(ACCESS_PROFILE)))
            && ddi.getString(CASE_TYPE_ID).equalsIgnoreCase(item.getString(CASE_TYPE_ID))
            && ddi.getString(CASE_FIELD_ID).equalsIgnoreCase(item.getString(CASE_FIELD_ID))
            && (StringUtils.isNotEmpty(ddi.getString(LIST_ELEMENT_CODE))
            ? ddi.getString(LIST_ELEMENT_CODE).equalsIgnoreCase(item.getString(LIST_ELEMENT_CODE))
            : StringUtils.isEmpty(item.getString(LIST_ELEMENT_CODE))))
            .count() > 1;
    }

    private boolean hasDuplicateRowsForSearchCases(List<DefinitionDataItem> layoutItems, DefinitionDataItem ddi) {
        return layoutItems.stream().filter(item -> (StringUtils.isNotEmpty(ddi.getString(ACCESS_PROFILE))
            ? ddi.getString(ACCESS_PROFILE).equalsIgnoreCase(item.getString(ACCESS_PROFILE))
            : StringUtils.isEmpty(item.getString(ACCESS_PROFILE)))
            && ddi.getString(CASE_TYPE_ID).equalsIgnoreCase(item.getString(CASE_TYPE_ID))
            && ddi.getString(CASE_FIELD_ID).equalsIgnoreCase(item.getString(CASE_FIELD_ID))
            && (StringUtils.isNotEmpty(ddi.getString(LIST_ELEMENT_CODE))
            ? ddi.getString(LIST_ELEMENT_CODE).equalsIgnoreCase(item.getString(LIST_ELEMENT_CODE))
            : StringUtils.isEmpty(item.getString(LIST_ELEMENT_CODE)))
            && ddi.getString(USE_CASE).equalsIgnoreCase(item.getString(USE_CASE))).count() > 1;
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
        layoutEntity.setDisplayContextParameter(definition.getString(ColumnName.DISPLAY_CONTEXT_PARAMETER));
        final String accessProfile = definition.getString(ACCESS_PROFILE);
        if (StringUtils.isNotEmpty(accessProfile)) {
            layoutEntity.setAccessProfile(
                getAccessProfileEntity(layoutEntity, definition.getSheetName(), accessProfile));
        }
        final String sortOrder = definition.getString(RESULTS_ORDERING);
        if (StringUtils.isNotEmpty(sortOrder)) {
            populateSortOrder(layoutEntity, sortOrder);
        }
        final String showCondition = definition.getString(FIELD_SHOW_CONDITION);
        if (StringUtils.isNotEmpty(showCondition)) {
            populateShowCondition(layoutEntity, this.parseShowCondition(showCondition));
        }
        final String useCase = definition.getString(USE_CASE);
        if (StringUtils.isNotEmpty(useCase)) {
            populateUseCase(layoutEntity, useCase);
        }
        entityToDefinitionDataItemRegistry.addDefinitionDataItemForEntity(layoutEntity, definition);
        return ParseResult.Entry.createNew(layoutEntity);
    }

    private AccessProfileEntity getAccessProfileEntity(GenericLayoutEntity layoutEntity,
                                                       String sheetName,
                                                       String accessProfile) {
        return parseContext.getAccessProfile(accessProfile)
            .orElseThrow(() -> new MapperException(String.format(
                "- Unknown access profile '%s' in worksheet '%s' for caseField '%s'",
                accessProfile, sheetName, layoutEntity.getCaseField().getReference())));
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
            throw new MapperException(String.format(
                "Invalid results ordering pattern '%s' in worksheet '%s' for caseType '%s' for caseField '%s'",
                dataItem.getString(RESULTS_ORDERING), dataItem.getSheetName(), caseTyeReference,
                dataItem.getString(CASE_FIELD_ID)));
        }
    }

    private void validateDuplicateAndGaps(CaseTypeEntity caseType,
                                          String sheetName,
                                          List<DefinitionDataItem> sortDataItems) {
        Map<String, List<Integer>> sortPrioritiesByAccessProfile = getSortPrioritiesByAccessProfile(sortDataItems);
        sortPrioritiesByAccessProfile.values().forEach(items -> {
            checkDuplicateSortOrders(items, sheetName, caseType.getReference());
            checkGapsInSortPriority(items, sheetName, caseType.getReference());
        });
    }

    private void checkGapsInSortPriority(List<Integer> items, String sheetName, String caseType) {
        boolean noGaps = items.stream().allMatch(e -> items
            .containsAll(IntStream.range(1, e).boxed().collect(Collectors.toList())));
        if (!noGaps) {
            throw new MapperException(String.format(
                "Missing sort order priority in worksheet '%s' for caseType '%s'", sheetName, caseType));
        }
    }

    private void checkDuplicateSortOrders(List<Integer> items, String sheetName, String caseType) {
        boolean isUnique = items.stream().allMatch(new HashSet<>()::add);
        if (!isUnique) {
            throw new MapperException(String.format(
                "Duplicate sort order priority in worksheet '%s' for caseType '%s'", sheetName, caseType));
        }
    }

    private Map<String, List<Integer>> getSortPrioritiesByAccessProfile(List<DefinitionDataItem> sortDataItems) {
        Map<String, List<Integer>> sortPrioritiesByAccessProfile = new HashMap<>();

        sortDataItems.stream().forEach(ddi -> {
            String key = StringUtils.isNotEmpty(ddi.getString(ACCESS_PROFILE))
                ? ddi.getString(ACCESS_PROFILE) : ALL_ACCESS_PROFILES;
            key = ddi.getSheetName().equals(SheetName.SEARCH_CASES_RESULT_FIELDS.getName())
                ? key + "_" + ddi.getUseCase() : key;
            List<Integer> priorities = sortPrioritiesByAccessProfile.getOrDefault(key, new ArrayList<>());
            priorities.add(Integer.valueOf(ddi.getString(RESULTS_ORDERING).split(SORT_STRING_DELIMITER)[0]));
            sortPrioritiesByAccessProfile.put(key, priorities);
        });
        sortPrioritiesByAccessProfile.keySet().forEach(key -> {
            if (!key.equalsIgnoreCase(ALL_ACCESS_PROFILES)
                && sortPrioritiesByAccessProfile.get(ALL_ACCESS_PROFILES) != null) {

                sortPrioritiesByAccessProfile.get(key).addAll(sortPrioritiesByAccessProfile.get(ALL_ACCESS_PROFILES));
            }
        });
        return sortPrioritiesByAccessProfile;
    }

    protected static SortOrder getSortOrder(String sortOrderString) {
        String[] tokens = sortOrderString.split(SORT_STRING_DELIMITER);
        return new SortOrder(Integer.valueOf(tokens[0]), tokens[1]);
    }

    protected Logger getLogger() {
        return logger;
    }

    public ShowConditionParser getShowConditionParser() {
        return showConditionParser;
    }

    protected abstract void populateSortOrder(GenericLayoutEntity layoutEntity, String sortOrde);

    protected abstract DefinitionSheet getDefinitionSheet(Map<String, DefinitionSheet> definitionSheets);

    protected abstract String getLayoutName();

    protected abstract GenericLayoutEntity createLayoutCaseFieldEntity();

    protected abstract void populateShowCondition(GenericLayoutEntity layoutEntity, String showCondition);

    protected abstract void populateUseCase(GenericLayoutEntity layoutEntity, String showCondition);
}
