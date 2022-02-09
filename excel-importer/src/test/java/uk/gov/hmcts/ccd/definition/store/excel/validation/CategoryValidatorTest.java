package uk.gov.hmcts.ccd.definition.store.excel.validation;

import com.google.common.collect.Lists;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

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
    private static final String ERROR_CASE_TYPE_PARENT = "CategoryTab Invalid ParentCategoryID";
    private static final String CASE_TYPE_NOT_FOUND = "It cannot be found in the spreadsheet";

    private CategoryValidator categoryValidator;
    private DefinitionDataItem definitionDataItem;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        parseContext = new ParseContext();
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
        definitionDataItem.addAttribute(ColumnName.PARENTCATEGORY_ID, parentCategoryId);
        return definitionDataItem;
    }

    // Example 1- validly defined Categories tab is as below
    @Test
    public void testValidateCategoryTab() {
        categoryValidator.validate(parseContext, Lists.newArrayList(definitionDataItem));
    }

    // Example 6 - Invalid definition - Same categoryID defined in the sub-categories
    @Test(expected = InvalidImportException.class)
    public void failSameCategoryIDDefinedForSubCategories() {
        try {
            categoryValidator.validate(parseContext, Lists.newArrayList(
                definitionDataItem,

                buildDefinitionDataItem(LIVE_FROM, LIVE_TO, CASE_TYPE,
                    "translatedEvidence", "translatedEvidence", 120, CATEGORY_ID),

                buildDefinitionDataItem(LIVE_FROM, LIVE_TO, CASE_TYPE,
                    "translatedEvidence", "XXX", 120, CATEGORY_ID)
                )
            );
        } catch (Exception exception) {
            assertThat(exception.getMessage(), containsString(DUPLICATED_CATEGORY_ID_ERROR));
            throw exception;
        }
    }

    // Example 5 - Invalid definition - Same categoryID defined in the main categories
    @Test(expected = InvalidImportException.class)
    public void failSameCategoryIdDefinedForCategories() {
        try {
            categoryValidator.validate(parseContext, Lists.newArrayList(
                definitionDataItem,

                buildDefinitionDataItem(LIVE_FROM, LIVE_TO, CASE_TYPE,
                    CATEGORY_ID, CATEGORY_LABEL, DISPLAY_ORDER, NO_PARENT),

                buildDefinitionDataItem(LIVE_FROM, LIVE_TO, CASE_TYPE,
                    "translatedEvidence", "translatedEvidence", 120, NO_PARENT),

                buildDefinitionDataItem(LIVE_FROM, LIVE_TO, CASE_TYPE,
                    "translatedEvidence", "XXX", 120, NO_PARENT)
                )
            );
        } catch (Exception exception) {
            assertThat(exception.getMessage(), containsString(DUPLICATED_CATEGORY_ID_ERROR));
            throw exception;
        }
    }

    // Example 4 - Invalid definition - Non Unique Display Order in the sub-categories
    @Test(expected = InvalidImportException.class)
    public void failNonUniqueDisplayOrderForSubCategories() {
        try {
            categoryValidator.validate(parseContext, Lists.newArrayList(
                definitionDataItem,

                buildDefinitionDataItem(LIVE_FROM, LIVE_TO, CASE_TYPE,
                    "translatedEvidence", "translatedEvidence", 110, CATEGORY_ID),

                buildDefinitionDataItem(LIVE_FROM, LIVE_TO, CASE_TYPE,
                    "divorceDocs", "divorceDocs", 110, CATEGORY_ID)
                )
            );
        } catch (Exception exception) {
            assertThat(exception.getMessage(), containsString(DISPLAY_ERROR));
            throw exception;
        }
    }

    // Example 3 - Invalid definition - Non Unique Display Order on the main categories
    @Test(expected = InvalidImportException.class)
    public void failNonUniqueDisplayOrderMainCategories() {
        try {
            categoryValidator.validate(parseContext, Lists.newArrayList(
                definitionDataItem,

                buildDefinitionDataItem(LIVE_FROM, LIVE_TO, CASE_TYPE,
                    "translatedEvidence", "translatedEvidence", 110, NO_PARENT),

                buildDefinitionDataItem(LIVE_FROM, LIVE_TO, CASE_TYPE,
                    "divorceDocs", "divorceDocs", 110, NO_PARENT)
                )
            );
        } catch (Exception exception) {
            assertThat(exception.getMessage(), containsString(DISPLAY_ERROR));
            throw exception;
        }
    }

    // Example 2 - Invalid definition - reference to parent category id of another case type
    @Test(expected = InvalidImportException.class)
    public void failForReferenceToParentCategoryIdOfCaseType() {
        try {
            categoryValidator.validate(parseContext, Lists.newArrayList(
                definitionDataItem,

                buildDefinitionDataItem(LIVE_FROM, LIVE_TO, "FT_MultiplePages",
                    "translatedEvidence", "translatedEvidence", 110, NO_PARENT),

                buildDefinitionDataItem(LIVE_FROM, LIVE_TO, CASE_TYPE,
                    "divorceDocs", "divorceDocs", 150, "translatedEvidence")
                )
            );
        } catch (Exception exception) {
            assertThat(exception.getMessage(), containsString(ERROR_CASE_TYPE_PARENT));
            throw exception;
        }
    }


    // Invalid definition - Case type
    @Test(expected = InvalidImportException.class)
    public void failForReferenceInvalidCaseType() {
        try {
            categoryValidator.validate(parseContext, Lists.newArrayList(
                definitionDataItem,

                buildDefinitionDataItem(LIVE_FROM, LIVE_TO, "TUTUT",
                    "translatedEvidence", "translatedEvidence", 110, NO_PARENT),

                buildDefinitionDataItem(LIVE_FROM, LIVE_TO, CASE_TYPE,
                    "divorceDocs", "divorceDocs", 150, "translatedEvidence")
                )
            );
        } catch (Exception exception) {
            assertThat(exception.getMessage(), containsString(CASE_TYPE_NOT_FOUND));
            throw exception;
        }
    }
}
