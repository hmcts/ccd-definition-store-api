package uk.gov.hmcts.ccd.definition.store.excel.util.mapper;


import uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapperUtilTest {

    private DefinitionDataItem item;

    @BeforeEach
    void setup() {
        item = new DefinitionDataItem();
    }

    @Test
    void getBooleanTrue() {
        item.addAttribute(ColumnName.DEFAULT_HIDDEN.toString(), "tRuE");
        assertTrue(MapperUtil.getBoolean(item, SheetName.CASE_EVENT_TO_FIELDS, ColumnName.DEFAULT_HIDDEN));
    }

    @Test
    void getBooleanUpperCaseT() {
        item.addAttribute(ColumnName.DEFAULT_HIDDEN.toString(), "T");
        assertTrue(MapperUtil.getBoolean(item, SheetName.CASE_EVENT_TO_FIELDS, ColumnName.DEFAULT_HIDDEN));
    }

    @Test
    void getBooleanLowerCaseT() {
        item.addAttribute(ColumnName.DEFAULT_HIDDEN.toString(), "t");
        assertTrue(MapperUtil.getBoolean(item, SheetName.CASE_EVENT_TO_FIELDS, ColumnName.DEFAULT_HIDDEN));
    }

    @Test
    void getBooleanYes() {
        item.addAttribute(ColumnName.DEFAULT_HIDDEN.toString(), "yEs");
        assertTrue(MapperUtil.getBoolean(item, SheetName.CASE_EVENT_TO_FIELDS, ColumnName.DEFAULT_HIDDEN));
    }

    @Test
    void getBooleanUpperCaseY() {
        item.addAttribute(ColumnName.DEFAULT_HIDDEN.toString(), "Y");
        assertTrue(MapperUtil.getBoolean(item, SheetName.CASE_EVENT_TO_FIELDS, ColumnName.DEFAULT_HIDDEN));
    }

    @Test
    void getBooleanLowerCaseY() {
        item.addAttribute(ColumnName.DEFAULT_HIDDEN.toString(), "y");
        assertTrue(MapperUtil.getBoolean(item, SheetName.CASE_EVENT_TO_FIELDS, ColumnName.DEFAULT_HIDDEN));
    }

    @Test
    void getBooleanFalse() {
        item.addAttribute(ColumnName.DEFAULT_HIDDEN.toString(), "fALse");
        assertFalse(MapperUtil.getBoolean(item, SheetName.CASE_EVENT_TO_FIELDS, ColumnName.DEFAULT_HIDDEN));
    }

    @Test
    void getBooleanNative() {
        item.addAttribute(ColumnName.DEFAULT_HIDDEN.toString(), Boolean.FALSE);
        assertFalse(MapperUtil.getBoolean(item, SheetName.CASE_EVENT_TO_FIELDS, ColumnName.DEFAULT_HIDDEN));
    }

    @Test
    void getBooleanUpperCaseF() {
        item.addAttribute(ColumnName.DEFAULT_HIDDEN.toString(), "F");
        assertFalse(MapperUtil.getBoolean(item, SheetName.CASE_EVENT_TO_FIELDS, ColumnName.DEFAULT_HIDDEN));
    }

    @Test
    void getBooleanLowerCaseF() {
        item.addAttribute(ColumnName.DEFAULT_HIDDEN.toString(), "F");
        assertFalse(MapperUtil.getBoolean(item, SheetName.CASE_EVENT_TO_FIELDS, ColumnName.DEFAULT_HIDDEN));
    }

    @Test
    void getBooleanNo() {
        item.addAttribute(ColumnName.DEFAULT_HIDDEN.toString(), "nO");
        assertFalse(MapperUtil.getBoolean(item, SheetName.CASE_EVENT_TO_FIELDS, ColumnName.DEFAULT_HIDDEN));
    }

    @Test
    void getBooleanUpperCaseN() {
        item.addAttribute(ColumnName.DEFAULT_HIDDEN.toString(), "N");
        assertFalse(MapperUtil.getBoolean(item, SheetName.CASE_EVENT_TO_FIELDS, ColumnName.DEFAULT_HIDDEN));
    }

    @Test
    void getBooleanLowerCaseN() {
        item.addAttribute(ColumnName.DEFAULT_HIDDEN.toString(), "n");
        assertFalse(MapperUtil.getBoolean(item, SheetName.CASE_EVENT_TO_FIELDS, ColumnName.DEFAULT_HIDDEN));
    }

    @Test
    void getBooleanNoValueButMandatory() {
        assertThrows(MapperException.class, () -> 
            MapperUtil.getBoolean(item, SheetName.CASE_EVENT_TO_FIELDS, ColumnName.PAGE_ID));
    }

    @Test
    void getBooleanNoValueOptional() {
        assertNull(MapperUtil.getBoolean(item, SheetName.CASE_EVENT_TO_FIELDS, ColumnName.HINT_TEXT));
    }

    @Test
    void getBooleanBlankValueButMandatory() {
        item.addAttribute(ColumnName.DISPLAY_CONTEXT.toString(), "   ");
        MapperException ex = assertThrows(MapperException.class, () -> 
            MapperUtil.getBoolean(item, SheetName.CASE_EVENT_TO_FIELDS, ColumnName.PAGE_ID));
        assertEquals(String.format("Couldn't find the column '%s' or invalid value in the sheet '%s'",
            ColumnName.PAGE_ID,
            SheetName.CASE_EVENT_TO_FIELDS),
            ex.getMessage());
    }

    @Test
    void getBooleanBlankValueOptional() {
        item.addAttribute(ColumnName.DEFAULT_HIDDEN.toString(), "   ");
        assertNull(MapperUtil.getBoolean(item, SheetName.CASE_EVENT_TO_FIELDS, ColumnName.HINT_TEXT));
    }

    @Test
    void getBooleanValueNotPredefined() {
        item.addAttribute(ColumnName.DISPLAY_CONTEXT.toString(), "Dog");
        MapperException ex = assertThrows(MapperException.class, () -> 
            MapperUtil.getBoolean(item, SheetName.CASE_EVENT_TO_FIELDS, ColumnName.DISPLAY_CONTEXT));
        assertEquals(String.format("Invalid value 'Dog' is found in column '%s' in the sheet '%s'",
            ColumnName.DISPLAY_CONTEXT,
            SheetName.CASE_EVENT_TO_FIELDS),
            ex.getMessage());
    }

    @Test
    void findSheet() {
        final DefinitionSheet sheet = new DefinitionSheet();
        sheet.setName(SheetName.CASE_TYPE.getName());
        final List<DefinitionSheet> sheets = new ArrayList<>();
        sheets.add(sheet);
        final DefinitionSheet found = MapperUtil.findSheet(sheets, SheetName.CASE_TYPE);
        assertThat(found, is(sheet));
    }

    @Test
    void getString() {
        final DefinitionSheet sheet = new DefinitionSheet();
        sheet.setName(SheetName.CASE_TYPE.getName());
        item.addAttribute(ColumnName.DISPLAY_CONTEXT, "Dog");
        sheet.addDataItem(item);
        assertThat(MapperUtil.getString(item, SheetName.CASE_TYPE, ColumnName.DISPLAY_CONTEXT), is("Dog"));
    }

    @Test
    void missingNonMandatoryStringAttribute() {
        final DefinitionSheet sheet = new DefinitionSheet();
        sheet.setName(SheetName.CASE_TYPE.getName());
        assertNull(MapperUtil.getString(item, SheetName.CASE_TYPE, ColumnName.CASE_TYPE_ID));
    }

    @Test
    void missingMandatoryStringAttribute() {
        final DefinitionSheet sheet = new DefinitionSheet();
        sheet.setName(SheetName.CASE_TYPE.getName());
        assertThrows(MapperException.class, () -> 
            MapperUtil.getString(item, SheetName.CASE_TYPE, ColumnName.ID));
    }

    @Test
    void getInteger() {
        final DefinitionSheet sheet = new DefinitionSheet();
        sheet.setName(SheetName.CASE_TYPE.getName());
        item.addAttribute(ColumnName.DISPLAY_CONTEXT, 0.0);
        sheet.addDataItem(item);
        assertThat(MapperUtil.getInteger(item, SheetName.CASE_TYPE, ColumnName.DISPLAY_CONTEXT), is(0));
    }

    @Test
    void getBigDecimal() {
        final DefinitionSheet sheet = new DefinitionSheet();
        sheet.setName(SheetName.CASE_TYPE.getName());
        item.addAttribute(ColumnName.DISPLAY_CONTEXT, 0.0);
        sheet.addDataItem(item);
        assertThat(MapperUtil.getBigDecimal(item, SheetName.CASE_TYPE, ColumnName.DISPLAY_CONTEXT),
            is(BigDecimal.valueOf(0.0)));
    }

    @Test
    void getIntegerList() {
        final DefinitionSheet sheet = new DefinitionSheet();
        sheet.setName(SheetName.CASE_TYPE.getName());
        item.addAttribute(ColumnName.DISPLAY_CONTEXT, "3  ,5, 8,13,21");
        sheet.addDataItem(item);
        final List<Integer> integerList = MapperUtil.getIntegerList(
            item, SheetName.CASE_TYPE, ColumnName.DISPLAY_CONTEXT);
        assertThat(integerList, contains(3, 5, 8, 13, 21));
    }

    @Test
    void expectsMapperException_whenIntegerListsHasNonNumber() {
        final DefinitionSheet sheet = new DefinitionSheet();
        sheet.setName(SheetName.CASE_TYPE.getName());
        item.addAttribute(ColumnName.DISPLAY_CONTEXT, "3,a,8");
        sheet.addDataItem(item);
        assertThrows(MapperException.class, () -> 
            MapperUtil.getIntegerList(item, SheetName.CASE_TYPE, ColumnName.DISPLAY_CONTEXT));
    }

    @Test
    void emptyIntegerList() {
        final DefinitionSheet sheet = new DefinitionSheet();
        sheet.setName(SheetName.CASE_TYPE.getName());
        sheet.addDataItem(item);
        final List<Integer> integerList = MapperUtil.getIntegerList(
            item, SheetName.CASE_TYPE, ColumnName.DISPLAY_CONTEXT);
        assertThat(integerList, empty());
    }

}
