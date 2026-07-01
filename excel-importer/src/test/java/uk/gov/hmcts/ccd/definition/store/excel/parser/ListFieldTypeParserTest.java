package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.excel.validation.SpreadsheetValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeListItemEntity;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_FIXED_LIST;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_MULTI_SELECT_LIST;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_RADIO_FIXED_LIST;

@ExtendWith(MockitoExtension.class)
class ListFieldTypeParserTest extends ParserTestBase {

    @Mock
    private FieldTypeEntity fieldFixedList;

    @Mock
    private FieldTypeEntity fieldFixedRadioList;

    @Mock
    private FieldTypeEntity fieldMultiList;

    @Mock
    private SpreadsheetValidator spreadsheetValidator;

    private DefinitionSheet definitionSheet;

    @BeforeEach
    void setup() {

        init();

        parseContext = mock(ParseContext.class);
        definitionSheet = new DefinitionSheet();
    }

    @Test
    void shouldFail_whenNoBaseTypeFoundForFixedList() {
        given(parseContext.getBaseType(BASE_FIXED_LIST)).willReturn(Optional.empty());
        InvalidImportException ex = assertThrows(InvalidImportException.class, () ->
            new ListFieldTypeParser(parseContext, spreadsheetValidator));

        assertThat(ex.getMessage(), containsString("No base type found for: FixedList"));
    }

    @Test
    void shouldFail_whenNoBaseTypeFoundForMultiSelectList() {

        given(parseContext.getBaseType(BASE_FIXED_LIST)).willReturn(Optional.of(fieldFixedList));
        given(parseContext.getBaseType(BASE_RADIO_FIXED_LIST)).willReturn(Optional.of(fieldFixedRadioList));
        given(parseContext.getBaseType(BASE_MULTI_SELECT_LIST)).willReturn(Optional.empty());

        InvalidImportException ex = assertThrows(InvalidImportException.class, () ->
            new ListFieldTypeParser(parseContext, spreadsheetValidator));
        assertThat(ex.getMessage(), containsString("No base type found for: MultiSelectList"));
    }

    @Test
    void shouldParseEmptySheet() {

        given(parseContext.getBaseType(BASE_FIXED_LIST)).willReturn(Optional.of(fieldFixedList));
        given(parseContext.getBaseType(BASE_RADIO_FIXED_LIST)).willReturn(Optional.of(fieldFixedRadioList));
        given(parseContext.getBaseType(BASE_MULTI_SELECT_LIST)).willReturn(Optional.of(fieldMultiList));

        definitionSheets.put(SheetName.FIXED_LISTS.getName(), definitionSheet);

        final ListFieldTypeParser listFieldTypeParser = new ListFieldTypeParser(parseContext, spreadsheetValidator);
        final ParseResult<FieldTypeEntity> result = listFieldTypeParser.parse(definitionSheets);

        assertThat(result.getAllResults(), empty());
    }

    @Test
    void shouldParseListType() {

        given(parseContext.getBaseType(BASE_FIXED_LIST)).willReturn(Optional.of(fieldFixedList));
        given(parseContext.getBaseType(BASE_RADIO_FIXED_LIST)).willReturn(Optional.of(fieldFixedRadioList));
        given(parseContext.getBaseType(BASE_MULTI_SELECT_LIST)).willReturn(Optional.of(fieldMultiList));

        definitionSheets.put(SheetName.FIXED_LISTS.getName(), definitionSheet);
        final DefinitionDataItem dataItem = buildDefinitionDataItem();
        definitionSheet.addDataItem(dataItem);

        final ListFieldTypeParser listFieldTypeParser = new ListFieldTypeParser(parseContext, spreadsheetValidator);
        final ParseResult<FieldTypeEntity> result = listFieldTypeParser.parse(definitionSheets);

        final FieldTypeEntity parsed = result.getAllResults().get(0);
        assertThat(parsed.getVersion(), is(nullValue()));
        assertThat(parsed.getReference(), is("FixedList-ngitb"));

        final FieldTypeListItemEntity listItem = parsed.getListItems().get(0);
        assertThat(listItem.getValue(), is("NGITBEnum"));
        assertThat(listItem.getLabel(), is("NGITB"));
        assertThat(listItem.getOrder(), is(3));
    }

    @Test
    void shouldParseListTypeWithCaseTypeId_groupDataItemsByCaseTypeAndId() {
        given(parseContext.getBaseType(BASE_FIXED_LIST)).willReturn(Optional.of(fieldFixedList));
        given(parseContext.getBaseType(BASE_RADIO_FIXED_LIST)).willReturn(Optional.of(fieldFixedRadioList));
        given(parseContext.getBaseType(BASE_MULTI_SELECT_LIST)).willReturn(Optional.of(fieldMultiList));

        definitionSheets.put(SheetName.FIXED_LISTS.getName(), definitionSheet);
        DefinitionDataItem item1 = buildDefinitionDataItemWithCaseTypeId("mylist", "CaseTypeA", "code1", "Label1", 1);
        DefinitionDataItem item2 = buildDefinitionDataItemWithCaseTypeId("mylist", "CaseTypeB", "code1", "Label1", 1);
        definitionSheet.addDataItem(item1);
        definitionSheet.addDataItem(item2);

        ListFieldTypeParser listFieldTypeParser = new ListFieldTypeParser(parseContext, spreadsheetValidator);
        ParseResult<FieldTypeEntity> result = listFieldTypeParser.parse(definitionSheets);

        // groupDataItemsByCaseTypeAndId yields two groups: mylist-CaseTypeA,
        // mylist-CaseTypeB -> 2 list types * 3 (FixedList, FixedRadioList, MultiSelectList) = 6
        assertThat(result.getAllResults().size(), is(6));
        List<String> references = result.getAllResults().stream()
            .map(FieldTypeEntity::getReference)
            .toList();
        assertTrue(references.contains("FixedList-mylist-CaseTypeA"));
        assertTrue(references.contains("FixedList-mylist-CaseTypeB"));
    }

    @Test
    void shouldParseListTypeWithMultipleRowsInSameGroup_groupDataItemsByCaseTypeAndId() {
        given(parseContext.getBaseType(BASE_FIXED_LIST)).willReturn(Optional.of(fieldFixedList));
        given(parseContext.getBaseType(BASE_RADIO_FIXED_LIST)).willReturn(Optional.of(fieldFixedRadioList));
        given(parseContext.getBaseType(BASE_MULTI_SELECT_LIST)).willReturn(Optional.of(fieldMultiList));

        definitionSheets.put(SheetName.FIXED_LISTS.getName(), definitionSheet);
        DefinitionDataItem item1 = buildDefinitionDataItemWithCaseTypeId("list1", "CT1", "code1", "Label1", 1);
        DefinitionDataItem item2 = buildDefinitionDataItemWithCaseTypeId("list1", "CT1", "code2", "Label2", 2);
        definitionSheet.addDataItem(item1);
        definitionSheet.addDataItem(item2);

        ListFieldTypeParser listFieldTypeParser = new ListFieldTypeParser(parseContext, spreadsheetValidator);
        ParseResult<FieldTypeEntity> result = listFieldTypeParser.parse(definitionSheets);

        // One group list1-CT1 with 2 rows -> 3 list types
        // (FixedList, FixedRadioList, MultiSelectList), each with 2 list items
        assertThat(result.getAllResults().size(), is(3));
        FieldTypeEntity fixedList = result.getAllResults().stream()
            .filter(f -> "FixedList-list1-CT1".equals(f.getReference()))
            .findFirst()
            .orElseThrow();
        assertThat(fixedList.getListItems().size(), is(2));
    }

    private DefinitionDataItem buildDefinitionDataItem() {
        DefinitionDataItem item = new DefinitionDataItem(SheetName.FIXED_LISTS.getName());
        item.addAttribute(ColumnName.ID.toString(), "ngitb");
        item.addAttribute(ColumnName.LIST_ELEMENT_CODE.toString(), "NGITBEnum");
        item.addAttribute(ColumnName.LIST_ELEMENT.toString(), "NGITB");
        item.addAttribute(ColumnName.DISPLAY_ORDER.toString(), 3);
        return item;
    }

    private DefinitionDataItem buildDefinitionDataItemWithCaseTypeId(String id, String caseTypeId,
                                                                     String listCode, String listLabel, int order) {
        DefinitionDataItem item = new DefinitionDataItem(SheetName.FIXED_LISTS.getName());
        item.addAttribute(ColumnName.ID.toString(), id);
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), caseTypeId);
        item.addAttribute(ColumnName.LIST_ELEMENT_CODE.toString(), listCode);
        item.addAttribute(ColumnName.LIST_ELEMENT.toString(), listLabel);
        item.addAttribute(ColumnName.DISPLAY_ORDER.toString(), order);
        return item;
    }
}
