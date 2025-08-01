package uk.gov.hmcts.ccd.definition.store.excel.parser;

import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.excel.validation.SpreadsheetValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeListItemEntity;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    private DefinitionDataItem buildDefinitionDataItem() {
        DefinitionDataItem item = new DefinitionDataItem(SheetName.FIXED_LISTS.getName());
        item.addAttribute(ColumnName.ID.toString(), "ngitb");
        item.addAttribute(ColumnName.LIST_ELEMENT_CODE.toString(), "NGITBEnum");
        item.addAttribute(ColumnName.LIST_ELEMENT.toString(), "NGITB");
        item.addAttribute(ColumnName.DISPLAY_ORDER.toString(), 3);
        return item;
    }
}
