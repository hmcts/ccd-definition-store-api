package uk.gov.hmcts.ccd.definition.store.excel.parser;

import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class CaseFieldParserTest extends ParserTestBase {

    private CaseFieldParser caseFieldParser;

    private EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;

    @BeforeEach
    void setup() {

        init();

        parseContext = mock(ParseContext.class);
        entityToDefinitionDataItemRegistry = new EntityToDefinitionDataItemRegistry();
        caseFieldParser = new CaseFieldParser(parseContext, entityToDefinitionDataItemRegistry);
        caseType = new CaseTypeEntity();
        caseType.setReference(CASE_TYPE_UNDER_TEST);

        definitionSheets.put(SheetName.CASE_FIELD.getName(), definitionSheet);
    }

    @Test
    void shouldFail_whenNoFieldsAreDefinedForCaseType() {
        SpreadsheetParsingException ex = assertThrows(SpreadsheetParsingException.class, () ->
            caseFieldParser.parseAll(definitionSheets, caseType));
        assertThat(ex.getMessage(), containsString(
                "At least one case field must be defined for case type: Some Case Type"));
    }

    @Test
    void shouldParse() {
        final FieldTypeEntity field = new FieldTypeEntity();
        given(parseContext.getCaseFieldType(CASE_TYPE_UNDER_TEST, "Case_Field")).willReturn(field);

        DefinitionDataItem dataItem = buildDefinitionDataItemCaseField(CASE_TYPE_UNDER_TEST);
        definitionSheet.addDataItem(dataItem);

        final Collection<CaseFieldEntity> caseFieldEntities = caseFieldParser.parseAll(definitionSheets, caseType);
        assertThat(caseFieldEntities.size(), is(1));
        final CaseFieldEntity entity = new ArrayList<>(caseFieldEntities).get(0);
        assertThat(entity.getReference(), is("Case_Field"));
        assertThat(entity.getLabel(), is("Case Field"));
        assertThat(entity.getFieldType(), is(field));
        assertThat(entity.isSearchable(), is(true));
        assertThat(entity.getCategoryId(), is("Category Id"));
        assertThat(
            entityToDefinitionDataItemRegistry.getForEntity(entity), Matchers.is(Optional.of(dataItem)));
    }

    private DefinitionDataItem buildDefinitionDataItemCaseField(final String caseTypeId) {
        final DefinitionDataItem item = new DefinitionDataItem(SheetName.CASE_FIELD.toString());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), caseTypeId);
        item.addAttribute(ColumnName.ID.toString(), "Case_Field");
        item.addAttribute(ColumnName.LABEL.toString(), "Case Field");
        item.addAttribute(ColumnName.FIELD_TYPE.toString(), "Text");
        item.addAttribute(ColumnName.CATEGORY_ID.toString(), "Category Id");
        return item;
    }
}
