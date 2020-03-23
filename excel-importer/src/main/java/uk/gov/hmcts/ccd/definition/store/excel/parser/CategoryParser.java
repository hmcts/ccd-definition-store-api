package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CategoryEntity;

import java.util.*;

public class CategoryParser {
    private static final Logger logger = LoggerFactory.getLogger(CategoryParser.class);

    public Collection<CategoryEntity> parseAll(Map<String, DefinitionSheet> definitionSheets, CaseTypeEntity caseType) {
        final String caseTypeId = caseType.getReference();

        logger.debug("Parsing categories for case type {}...", caseTypeId);

        final DefinitionSheet definitionSheet = definitionSheets.get(SheetName.CATEGORY.getName());

        if (definitionSheet == null) {
            logger.debug("Worksheet 'Categories' not found in the workbook...");
            return Collections.emptyList();
        }

        final List<DefinitionDataItem> categoryItems = definitionSheets.get(SheetName.CATEGORY.getName())
            .groupDataItemsByCaseType()
            .get(caseTypeId);

        if (categoryItems == null) {
            logger.debug("No categories defined in the worksheet 'Categories' for CaseType {}...", caseTypeId);
            return Collections.emptyList();
        }

        logger.debug("Parsing categories for case type {}: {} categories detected", caseTypeId, categoryItems.size());

        final List<CategoryEntity> categories = new ArrayList<>();

        for (DefinitionDataItem categoryDefinition : categoryItems) {
            final String categoryId = categoryDefinition.getString(ColumnName.CATEGORY_ID);
            logger.debug("Parsing categories for case type {}: Parsing category {}...", caseTypeId, categoryId);

            categories.add(parseCategory(categoryId, caseType, categoryDefinition));

            logger.info("Parsing categories for case type {}: Parsing category {}: OK", caseTypeId, categoryId);
        }

        logger.info("Parsing categories for case type {}: OK: {} case categories parsed", caseTypeId, categories.size());

        return categories;
    }

    private CategoryEntity parseCategory(String categoryId, CaseTypeEntity caseType, DefinitionDataItem categoryDefinition) {
        final CategoryEntity category = new CategoryEntity();

        category.setCaseType(caseType);
        category.setGroupId(categoryDefinition.getString(ColumnName.CATEGORY_GROUP_ID));
        category.setGroupName(categoryDefinition.getString(ColumnName.CATEGORY_GROUP_NAME));
        category.setCategoryId(categoryId);
        category.setLabel(categoryDefinition.getString(ColumnName.CATEGORY_LABEL));
        category.setDisplayOrder(categoryDefinition.getInteger(ColumnName.CATEGORY_DISPLAY_ORDER));
        category.setParentCategoryId(categoryDefinition.getString(ColumnName.CATEGORY_PARENT_ID));

        return category;
    }
}
