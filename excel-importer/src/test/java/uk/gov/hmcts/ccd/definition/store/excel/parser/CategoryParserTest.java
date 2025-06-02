package uk.gov.hmcts.ccd.definition.store.excel.parser;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.excel.challengequestion.BaseChallengeQuestionTest;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.excel.validation.CategoryValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;


class CategoryParserTest extends BaseChallengeQuestionTest {

    private static final Date LIVE_FROM = new Date();
    private static final Date LIVE_TO = new Date();
    private static final String CASE_TYPE = "FT_MasterCaseType";
    private static final String CASE_TYPE2 = "FT_MultiplePages";
    private static final String CATEGORY_ID = "evidence";
    private static final String CATEGORY_LABEL = "evidence";
    private static final Integer DISPLAY_ORDER = 100;
    protected static final String NO_PARENT = "";


    @Mock
    private CategoryValidator categoryValidator;
    private CategoryParser categoryParser;
    private ParseContext parseContext;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        parseContext = buildParseContext();
        this.categoryParser = new CategoryParser(categoryValidator);
    }

    @Test
    void testParse() {
        val categoryEntities =
            categoryParser.parse(createDefinitionSheets(), parseContext);

        assertThat(categoryEntities.size(), is(1));
        assertThat(categoryEntities.get(0).getCategoryId(), is(CATEGORY_ID));
        assertThat(categoryEntities.get(0).getCategoryLabel(), is(CATEGORY_LABEL));
        assertThat(categoryEntities.get(0).getDisplayOrder(), is(DISPLAY_ORDER));
        assertThat(categoryEntities.get(0).getParentCategoryId(), is(NO_PARENT));
    }

    @Test
    void testParseRegisteredCategory() {
        val categoryEntities =
            categoryParser.parse(createDefinitionSheets(), parseContext);

        assertThat(parseContext.getCategory(CASE_TYPE, CATEGORY_ID), is(categoryEntities.get(0)));
    }

    @Test
    void failDueToDuplicatedIDs() {
        doThrow(new ValidationException(new ValidationResult()))
            .when(categoryValidator)
            .validate(any(), any());
        assertThrows(ValidationException.class, () ->
            categoryParser.parse(createDefinitionSheets(), parseContext));
    }


    protected DefinitionDataItem buildDefinitionDataItem(Date liveFrom, Date liveTo, String caseType,
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

    private Map<String, DefinitionSheet> createDefinitionSheets() {
        final Map<String, DefinitionSheet> map = new HashMap<>();
        val definitionSheet = new DefinitionSheet();
        val definitionDataItem = buildDefinitionDataItem(LIVE_FROM, LIVE_TO, CASE_TYPE, CATEGORY_ID,
            CATEGORY_LABEL, DISPLAY_ORDER, NO_PARENT);

        definitionSheet.setName(SheetName.CATEGORY.getName());
        definitionSheet.addDataItem(definitionDataItem);
        map.put(SheetName.CATEGORY.getName(), definitionSheet);
        return map;
    }
}
