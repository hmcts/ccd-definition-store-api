package uk.gov.hmcts.ccd.definition.store.excel.validation;

import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import com.google.common.collect.Lists;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CategoryValidatorTest {

    private ParseContext parseContext;
    private static final String LIVE_FROM = "DD/01/YYYY";
    private static final String LIVE_TO = "DD/01/YYYY";
    private static final String CASE_TYPE = "FT_MasterCaseType";
    private static final String CATEGORY_ID = "evidence";
    private static final String CATEGORY_LABEL = "evidence";
    private static final Integer DISPLAY_ORDER = 100;
    protected static final String NO_PARENT = "";
    private static final String DUPLICATED_CATEGORY_ID_ERROR = "cannot be duplicated within case type.";
    private static final String DISPLAY_ERROR = "DisplayOrder cannot be duplicated";
    private static final String CASE_TYPE2 = "FT_MultiplePages";
    private static final String ERROR_CASE_TYPE_PARENT = "Categories tab Invalid ParentCategoryID";
    private static final String CASE_TYPE_NOT_FOUND = "It cannot be found in the spreadsheet";
    private static final String INVALID_VALUE = "Categories tab Invalid value";

    private CategoryValidator categoryValidator;
    private DefinitionDataItem definitionDataItem;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        parseContext = buildParseContext();
        categoryValidator = new CategoryValidator();
        definitionDataItem = buildDefinitionDataItem(LIVE_FROM, LIVE_TO, CASE_TYPE, CATEGORY_ID,
            CATEGORY_LABEL, DISPLAY_ORDER, NO_PARENT);
    }

    protected ParseContext buildParseContext() {
        val parseContext = new ParseContext();
        val caseTypeEntity1 = new CaseTypeEntity();
        val caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference(CASE_TYPE);
        caseTypeEntity1.setReference(CASE_TYPE2);
        parseContext.registerCaseType(caseTypeEntity);
        parseContext.registerCaseType(caseTypeEntity1);
        return parseContext;
    }

    protected DefinitionDataItem buildDefinitionDataItem(String liveFrom, String liveTo, String caseType,
                                                         String categoryID, String categoryLabel, Integer displayOder,
                                                         String parentCategoryId) {

        val definitionDataItem = new DefinitionDataItem(SheetName.CATEGORY.toString());
        definitionDataItem.addAttribute(ColumnName.LIVE_FROM, liveFrom);
        definitionDataItem.addAttribute(ColumnName.LIVE_TO, liveTo);
        definitionDataItem.addAttribute(ColumnName.CASE_TYPE_ID, caseType);
        definitionDataItem.addAttribute(ColumnName.CATEGORY_ID, categoryID);
        definitionDataItem.addAttribute(ColumnName.DISPLAY_ORDER, displayOder);
        definitionDataItem.addAttribute(ColumnName.CATEGORY_LABEL, categoryLabel);
        definitionDataItem.addAttribute(ColumnName.PARENT_CATEGORY_ID, parentCategoryId);
        return definitionDataItem;
    }

    // Example 1- validly defined Categories tab is as below
    @Test
    public void testValidateCategoryTab() {
        categoryValidator.validate(parseContext, Lists.newArrayList(definitionDataItem));
    }

    // Example 1.1- valid defined Categories with many cases types with the same categories IDs
    @Test
    public void testValidateCategoryTabWithSameCategoriesIdsForDifferentCasesTypes() {
        categoryValidator.validate(parseContext, Lists.newArrayList(

            buildDefinitionDataItem(LIVE_FROM, LIVE_TO, CASE_TYPE2,
                CATEGORY_ID, "translatedEvidence", 120, CATEGORY_ID),

            definitionDataItem
        ));
    }

    // Example 6 - Invalid definition - Same categoryID defined in the sub-categories
    @Test
    public void failSameCategoryIDDefinedForSubCategories() {
        val invalidPropertyException = assertThrows(InvalidImportException.class, () ->

            categoryValidator.validate(parseContext, Lists.newArrayList(
                definitionDataItem,

                buildDefinitionDataItem(LIVE_FROM, LIVE_TO, CASE_TYPE,
                    "translatedEvidence", "translatedEvidence", 120, CATEGORY_ID),

                buildDefinitionDataItem(LIVE_FROM, LIVE_TO, CASE_TYPE,
                    "translatedEvidence", "XXX", 120, CATEGORY_ID)
                )
            )
        );
        assertThat(invalidPropertyException.getMessage(), containsString(DUPLICATED_CATEGORY_ID_ERROR));
    }

    // Example 5 - Invalid definition - Same categoryID defined in the main categories
    @Test
    public void failSameCategoryIdDefinedForCategories() {
        val invalidPropertyException = assertThrows(InvalidImportException.class, () ->
            categoryValidator.validate(parseContext, Lists.newArrayList(
                definitionDataItem,

                buildDefinitionDataItem(LIVE_FROM, LIVE_TO, CASE_TYPE,
                    CATEGORY_ID, CATEGORY_LABEL, DISPLAY_ORDER, NO_PARENT),

                buildDefinitionDataItem(LIVE_FROM, LIVE_TO, CASE_TYPE,
                    "translatedEvidence", "translatedEvidence", 120, NO_PARENT),

                buildDefinitionDataItem(LIVE_FROM, LIVE_TO, CASE_TYPE,
                    "translatedEvidence", "XXX", 120, NO_PARENT)
                )
            ));
        assertThat(invalidPropertyException.getMessage(), containsString(DUPLICATED_CATEGORY_ID_ERROR));
    }

    // Example 4 - Invalid definition - Non Unique Display Order in the sub-categories
    @Test
    public void failNonUniqueDisplayOrderForSubCategories() {
        val invalidPropertyException = assertThrows(InvalidImportException.class, () ->
            categoryValidator.validate(parseContext, Lists.newArrayList(
                definitionDataItem,

                buildDefinitionDataItem(LIVE_FROM, LIVE_TO, CASE_TYPE,
                    "translatedEvidence", "translatedEvidence", 110, CATEGORY_ID),

                buildDefinitionDataItem(LIVE_FROM, LIVE_TO, CASE_TYPE,
                    "divorceDocs", "divorceDocs", 110, CATEGORY_ID)
                )
            ));

        assertThat(invalidPropertyException.getMessage(), containsString(DISPLAY_ERROR));
    }

    // Example 3 - Invalid definition - Non Unique Display Order on the main categories
    @Test
    public void failNonUniqueDisplayOrderMainCategories() {
        val invalidPropertyException = assertThrows(InvalidImportException.class, () ->
            categoryValidator.validate(parseContext, Lists.newArrayList(
                definitionDataItem,

                buildDefinitionDataItem(LIVE_FROM, LIVE_TO, CASE_TYPE,
                    "translatedEvidence", "translatedEvidence", 110, NO_PARENT),

                buildDefinitionDataItem(LIVE_FROM, LIVE_TO, CASE_TYPE,
                    "divorceDocs", "divorceDocs", 110, NO_PARENT)
                )
            ));
        assertThat(invalidPropertyException.getMessage(), containsString(DISPLAY_ERROR));
    }

    // Example 2 - Invalid definition - reference to parent category id of another case type
    @Test
    public void failForReferenceToParentCategoryIdOfCaseType() {
        val invalidPropertyException = assertThrows(InvalidImportException.class, () ->
            categoryValidator.validate(parseContext, Lists.newArrayList(
                definitionDataItem,

                buildDefinitionDataItem(LIVE_FROM, LIVE_TO, "FT_MultiplePages",
                    "translatedEvidence", "translatedEvidence", 110, NO_PARENT),

                buildDefinitionDataItem(LIVE_FROM, LIVE_TO, CASE_TYPE,
                    "divorceDocs", "divorceDocs", 150, "translatedEvidence")
                )
            ));
        assertThat(invalidPropertyException.getMessage(), containsString(ERROR_CASE_TYPE_PARENT));
    }

    // Invalid definition - Case type
    @Test
    public void failForReferenceInvalidCaseType() {
        val invalidPropertyException = assertThrows(InvalidImportException.class, () ->
            categoryValidator.validate(parseContext, Lists.newArrayList(
                definitionDataItem,

                buildDefinitionDataItem(LIVE_FROM, LIVE_TO, "TUTUT",
                    "translatedEvidence", "translatedEvidence", 110, NO_PARENT),

                buildDefinitionDataItem(LIVE_FROM, LIVE_TO, CASE_TYPE,
                    "divorceDocs", "divorceDocs", 150, "translatedEvidence")
                )
            ));

        assertThat(invalidPropertyException.getMessage(), containsString(CASE_TYPE_NOT_FOUND));

    }

    // Invalid definition - CategoryID
    @Test
    public void failForReferenceInvalidCategoryID() {
        val invalidPropertyException = assertThrows(InvalidImportException.class, () ->
            categoryValidator.validate(parseContext, Lists.newArrayList(
                definitionDataItem,
                buildDefinitionDataItem(LIVE_FROM, LIVE_TO, CASE_TYPE,
                    null, "translatedEvidence", 110, NO_PARENT)
                )
        ));
        assertThat(invalidPropertyException.getMessage(), containsString(INVALID_VALUE));
    }

    // Invalid definition - Category label
    @Test
    public void failForReferenceInvalidCategoryLabel() {
        val invalidPropertyException = assertThrows(InvalidImportException.class, () ->
            categoryValidator.validate(parseContext, Lists.newArrayList(
                definitionDataItem,

                buildDefinitionDataItem(LIVE_FROM, LIVE_TO, CASE_TYPE,
                    "translatedEvidence", null, 110, NO_PARENT)
                )
            ));
        assertThat(invalidPropertyException.getMessage(), containsString(INVALID_VALUE));
    }

    // Invalid definition - Display order
    @Test
    public void failForInvalidDisplayOrder() {
        val invalidPropertyException = assertThrows(InvalidImportException.class, () ->
            categoryValidator.validate(parseContext, Lists.newArrayList(
                definitionDataItem,

                buildDefinitionDataItem(LIVE_FROM, LIVE_TO, CASE_TYPE,
                    "translatedEvidence", "translatedEvidence", null, NO_PARENT)
                )
            ));
        assertThat(invalidPropertyException.getMessage(), containsString(INVALID_VALUE));
    }

    // Invalid definition - Display order
    @Test
    public void failForNegativeDisplayOrder() {
        val invalidPropertyException = assertThrows(InvalidImportException.class, () ->
            categoryValidator.validate(parseContext, Lists.newArrayList(
                definitionDataItem,

                buildDefinitionDataItem(LIVE_FROM, LIVE_TO, CASE_TYPE,
                    "translatedEvidence", "translatedEvidence", -1, NO_PARENT)
                )
            ));
        assertThat(invalidPropertyException.getMessage(), containsString(INVALID_VALUE));
    }
}
