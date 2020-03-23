package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CategoryEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CATEGORY;

@DisplayName("Category Parser Test")
class CategoryParserTest {
    private static final String CASE_TYPE_ID = "DIVORCE";
    private static final String CATEGORY_GROUP_ID = "mainDivorceDocs";
    private static final String CATEGORY_GROUP_NAME = "Main Divorce Docs";
    private static final String CATEGORY_ID = "marriageCert";
    private static final String CATEGORY_PARENT_ID = "divorceDocs";
    private static final String CATEGORY_LABEL = "Marriage Certificate";
    private static final Integer CATEGORY_DISPLAY_ORDER = 201;

    private Map<String, DefinitionSheet> definitionSheets;
    private CaseTypeEntity caseTypeEntity;

    private CategoryParser underTest;

    @BeforeEach
    void init() {
        caseTypeEntity = buildCaseTypeEntity();

        definitionSheets = new HashMap<>();
        definitionSheets.put(CATEGORY.getName(), buildCategorySheet());

        underTest = new CategoryParser();
    }

    @Test
    @DisplayName("parse category entities")
    void testParseAll() {
        final Collection<CategoryEntity> categoryEntities = underTest.parseAll(definitionSheets, caseTypeEntity);

        assertAll(() -> assertThat(categoryEntities, hasSize(1)),
            () -> {
                final CategoryEntity categoryEntity = new ArrayList<>(categoryEntities).get(0);
                assertThat(categoryEntity.getCaseType(), is(caseTypeEntity));
                assertThat(categoryEntity.getGroupId(), is(CATEGORY_GROUP_ID));
                assertThat(categoryEntity.getGroupName(), is(CATEGORY_GROUP_NAME));
                assertThat(categoryEntity.getCategoryId(), is(CATEGORY_ID));
                assertThat(categoryEntity.getParentCategoryId(), is(CATEGORY_PARENT_ID));
                assertThat(categoryEntity.getLabel(), is(CATEGORY_LABEL));
                assertThat(categoryEntity.getDisplayOrder(), is(CATEGORY_DISPLAY_ORDER));
            });
    }

    @Test
    @DisplayName("parse definition without category sheet defined")
    void testParseAllWhenNoCategorySheetDefined() {
        definitionSheets = new HashMap<>();
        final Collection<CategoryEntity> categoryEntities = underTest.parseAll(definitionSheets, caseTypeEntity);

        assertThat(categoryEntities, hasSize(0));
    }

    @Test
    @DisplayName("parse definition when no items in category sheet defined")
    void testParseAllWhenNoItemsInCategorySheetDefined() {
        definitionSheets = new HashMap<>();
        definitionSheets.put(CATEGORY.getName(), new DefinitionSheet());

        final Collection<CategoryEntity> categoryEntities = underTest.parseAll(definitionSheets, caseTypeEntity);

        assertThat(categoryEntities, hasSize(0));
    }

    private static CaseTypeEntity buildCaseTypeEntity() {
        final CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference(CASE_TYPE_ID);
        return caseType;
    }

    private static DefinitionSheet buildCategorySheet() {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = buildCategoryItem();
        sheet.addDataItem(item);

        return sheet;
    }

    private static DefinitionDataItem buildCategoryItem() {
        final DefinitionDataItem item = new DefinitionDataItem(CATEGORY.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CATEGORY_GROUP_ID, CATEGORY_GROUP_ID);
        item.addAttribute(ColumnName.CATEGORY_GROUP_NAME, CATEGORY_GROUP_NAME);
        item.addAttribute(ColumnName.CATEGORY_ID, CATEGORY_ID);
        item.addAttribute(ColumnName.CATEGORY_LABEL, CATEGORY_LABEL);
        item.addAttribute(ColumnName.CATEGORY_DISPLAY_ORDER, CATEGORY_DISPLAY_ORDER);
        item.addAttribute(ColumnName.CATEGORY_PARENT_ID, CATEGORY_PARENT_ID);
        return item;
    }
}
