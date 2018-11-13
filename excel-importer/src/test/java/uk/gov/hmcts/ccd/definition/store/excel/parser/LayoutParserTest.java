package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.*;

@DisplayName("Layout Parser Test")
class LayoutParserTest {

    private LayoutParser underTest;
    private Map<String, DefinitionSheet> definitionSheets;

    private static final String CASE_TYPE_ID = "N>G>I>T>B.";
    private static final String CASE_FIELD_ID = "BaYaN";
    private CaseTypeEntity caseTypeEntity;
    private CaseFieldEntity caseFieldEntity;

    @BeforeEach
    void init() {

        caseTypeEntity = buildCaseTypeEntity();
        caseFieldEntity = buildCaseFieldEntity();

        final ParseContext context = new ParseContext();
        context.registerCaseType(caseTypeEntity);
        context.registerCaseFieldForCaseType(CASE_TYPE_ID, caseFieldEntity);

        final ShowConditionParser showConditionParser = new ShowConditionParser();
        final EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry = new
            EntityToDefinitionDataItemRegistry();
        underTest = new LayoutParser(new WorkbasketInputLayoutParser(context, entityToDefinitionDataItemRegistry),
            new WorkbasketLayoutParser(context, entityToDefinitionDataItemRegistry),
            new SearchInputLayoutParser(context, entityToDefinitionDataItemRegistry),
            new SearchResultLayoutParser(context, entityToDefinitionDataItemRegistry),
            new CaseTypeTabParser(context,
                showConditionParser,
                entityToDefinitionDataItemRegistry),
            new WizardPageParser(context,
                showConditionParser,
                entityToDefinitionDataItemRegistry));

        definitionSheets = new HashMap<>();
        definitionSheets.put(WORK_BASKET_RESULT_FIELDS.getName(), buildSheetForGenerics(WORK_BASKET_RESULT_FIELDS));
        definitionSheets.put(SEARCH_INPUT_FIELD.getName(), buildSheetForGenerics(SEARCH_INPUT_FIELD));
        definitionSheets.put(WORK_BASKET_INPUT_FIELD.getName(), buildSheetForGenerics(WORK_BASKET_INPUT_FIELD));
        definitionSheets.put(SEARCH_RESULT_FIELD.getName(), buildSheetForGenerics(SEARCH_RESULT_FIELD));
        definitionSheets.put(CASE_TYPE_TAB.getName(), buildSheetForDisplayGroups());
        definitionSheets.put(CASE_EVENT_TO_FIELDS.getName(), buildSheetForCaseEventToFieldS());
    }

    @Test
    @DisplayName("tests that parse results are adding up in parseAllGenerics")
    void parseAllGenerics() {
        final ParseResult<GenericLayoutEntity> parseResult = underTest.parseAllGenerics(definitionSheets);
        final List<GenericLayoutEntity> results = parseResult.getAllResults();
        assertAll(() -> assertThat(results.size(), is(4)),
            () -> assertGenericLayoutEntityIsPresent(results, WorkBasketInputCaseFieldEntity.class),
            () -> assertGenericLayoutEntityIsPresent(results, WorkBasketCaseFieldEntity.class),
            () -> assertGenericLayoutEntityIsPresent(results, SearchInputCaseFieldEntity.class),
            () -> assertGenericLayoutEntityIsPresent(results, SearchResultCaseFieldEntity.class));
    }

    @Test
    @DisplayName("tests that parse results are adding up in parseAllDisplayGroups")
    void parseAllDisplayGroups() {
        final ParseResult<DisplayGroupEntity> parseResult = underTest.parseAllDisplayGroups(definitionSheets);
        final List<DisplayGroupEntity> results = parseResult.getAllResults();
        assertAll(() -> assertThat(results.size(), is(2)),
            () -> assertDisplayGroupEntity(results,
                "SolicitorOverview",
                DisplayGroupType.TAB,
                DisplayGroupPurpose.VIEW),
            () -> assertDisplayGroupEntity(results,
                "office is that waypage ngi",
                DisplayGroupType.PAGE,
                DisplayGroupPurpose.EDIT));
    }

    private void assertGenericLayoutEntityIsPresent(final List<GenericLayoutEntity> results, Class<?> clazz) {
        final GenericLayoutEntity entity = results.stream()
            .filter(o -> o.getClass() == clazz)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("object of class " + clazz + " is not found"));
        assertThat(entity.getCaseType(), is(caseTypeEntity));
        assertThat(entity.getCaseField(), is(caseFieldEntity));
    }

    private void assertDisplayGroupEntity(final List<DisplayGroupEntity> results,
                                          final String reference,
                                          final DisplayGroupType type,
                                          final DisplayGroupPurpose purpose) {
        final DisplayGroupEntity entity = results.stream()
            .filter(o -> StringUtils.equals(reference,
                o.getReference()) && type == o.getType() && purpose == o.getPurpose())
            .findFirst()
            .orElseThrow(() -> new RuntimeException("entity is not found by '" + reference + "'"));
        assertThat(entity.getCaseType(), is(caseTypeEntity));
        assertThat(entity.getDisplayGroupCaseFields().size(), is(1));
        assertThat(entity.getDisplayGroupCaseFields().get(0).getCaseField(), is(caseFieldEntity));
    }

    private CaseTypeEntity buildCaseTypeEntity() {
        final CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference(CASE_TYPE_ID);
        return caseType;
    }

    private CaseFieldEntity buildCaseFieldEntity() {
        final CaseFieldEntity caseField = new CaseFieldEntity();
        caseField.setReference(CASE_FIELD_ID);
        return caseField;
    }

    private DefinitionSheet buildSheetForGenerics(final SheetName sheetName) {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(sheetName.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID);
        item.addAttribute(ColumnName.DISPLAY_ORDER, 3.0);
        sheet.addDataItem(item);
        return sheet;
    }

    private DefinitionSheet buildSheetForDisplayGroups() {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(CASE_TYPE_TAB.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID);
        item.addAttribute(ColumnName.CHANNEL, "CaseWorker");
        item.addAttribute(ColumnName.TAB_ID, "SolicitorOverview");
        item.addAttribute(ColumnName.TAB_LABEL, "Overview");
        item.addAttribute(ColumnName.TAB_DISPLAY_ORDER, 1.0);
        item.addAttribute(ColumnName.TAB_FIELD_DISPLAY_ORDER, 5.0);
        sheet.addDataItem(item);
        return sheet;
    }

    private DefinitionSheet buildSheetForCaseEventToFieldS() {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(CASE_EVENT_TO_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID);
        item.addAttribute(ColumnName.CASE_EVENT_ID, "office is that way");
        item.addAttribute(ColumnName.PAGE_ID, "page ngi");
        sheet.addDataItem(item);
        return sheet;
    }
}
