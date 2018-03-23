package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.WORK_BASKET_RESULT_FIELDS;

@DisplayName("Generic Layout Parser Test")
public class GenericLayoutParserTest {
    private static final String INVALID_CASE_TYPE_ID = "Invalid Case Type";
    private static final String CASE_TYPE_ID = "Valid Case Type";
    private static final String CASE_TYPE_ID2 = "Valid Case Type II";
    private static final String CASE_FIELD_ID = "Some Field";
    private GenericLayoutParser classUnderTest;
    private Map<String, DefinitionSheet> definitionSheets;
    private EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;

    @Before
    public void setup() {
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference(CASE_TYPE_ID);
        CaseTypeEntity caseTypeEntity2 = new CaseTypeEntity();
        caseTypeEntity2.setReference(CASE_TYPE_ID2);
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setReference(CASE_FIELD_ID);

        final ParseContext context = new ParseContext();
        context.registerCaseType(caseTypeEntity);
        context.registerCaseType(caseTypeEntity2);
        context.registerCaseFieldForCaseType(CASE_TYPE_ID, caseFieldEntity);

        definitionSheets = new HashMap<>();

        entityToDefinitionDataItemRegistry = new EntityToDefinitionDataItemRegistry();
        classUnderTest = new WorkbasketLayoutParser(context, entityToDefinitionDataItemRegistry);
    }

    @Test(expected = MapperException.class)
    @DisplayName("Unknown definitions should generate error")
    public void shouldFailIfUnknownCaseType() {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID);
        item.addAttribute(ColumnName.DISPLAY_ORDER, 3.0);
        sheet.addDataItem(item);
        final DefinitionDataItem item2 = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID, INVALID_CASE_TYPE_ID);
        item2.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID);
        item2.addAttribute(ColumnName.DISPLAY_ORDER, 1.0);
        sheet.addDataItem(item2);
        definitionSheets.put(WORK_BASKET_RESULT_FIELDS.getName(), sheet);
        classUnderTest.parseAll(definitionSheets);
    }

    @Test(expected = SpreadsheetParsingException.class)
    @DisplayName("Unknown definitions should generate error")
    public void shouldFailIfCaseTypeHasNoDefinition() {
        final DefinitionSheet sheet = new DefinitionSheet();
        final DefinitionDataItem item = new DefinitionDataItem(WORK_BASKET_RESULT_FIELDS.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_ID);
        item.addAttribute(ColumnName.CASE_FIELD_ID, CASE_FIELD_ID);
        item.addAttribute(ColumnName.DISPLAY_ORDER, 3.0);
        sheet.addDataItem(item);
        definitionSheets.put(WORK_BASKET_RESULT_FIELDS.getName(), sheet);
        classUnderTest.parseAll(definitionSheets);
    }
}
