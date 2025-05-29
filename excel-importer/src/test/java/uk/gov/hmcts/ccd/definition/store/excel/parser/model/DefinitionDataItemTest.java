package uk.gov.hmcts.ccd.definition.store.excel.parser.model;

import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DefinitionDataItemTest {

    private DefinitionDataItem item;

    @BeforeEach
    public void setup() {
        item = new DefinitionDataItem(SheetName.CASE_EVENT.toString());
    }

    @Test
    public void shouldFail_whenRequiredAttributeDoesNotExist() {
        MapperException ex = assertThrows(MapperException.class, () -> {
            item.findAttribute(ColumnName.ID);
        });
        assertThat(ex.getMessage(), containsString(
            "Couldn't find the column ID in the sheet CaseEvent"));
    }

    @Test
    public void shouldFail_whenRequiredAttributeIsBlank() {
        item.addAttribute(ColumnName.ID.toString(), "");

        MapperException ex = assertThrows(MapperException.class, () -> {
            item.findAttribute(ColumnName.ID);
        });
        
        assertThat(ex.getMessage(), containsString(
            "There's a missing value in the column 'ID' or invalid value in the sheet 'CaseEvent'"));
    }

    @Test
    public void shouldSucceed_whenRequiredAttributeKeyIsUsingAnAlias() {
        item.addAttribute(ColumnName.ACCESS_PROFILE.getAliases()[0], "Access Profile Value");

        assertEquals("Access Profile Value", item.findAttribute(ColumnName.ACCESS_PROFILE));
    }

    @Test
    public void shouldGetNull_whenBigDecimalAttributeDoesNotExist() {
        final BigDecimal result = item.getBigDecimal(ColumnName.DISPLAY_ORDER);
        assertThat(result, is(nullValue()));
    }

    @Test
    public void shouldGetBigDecimal() {
        item.addAttribute(ColumnName.DISPLAY_ORDER, 1.0);

        final BigDecimal result = item.getBigDecimal(ColumnName.DISPLAY_ORDER);
        assertThat(result, is(BigDecimal.valueOf(1.0)));
    }

    @Test
    public void shouldFail_whenInvalidBigDecimal() {
        item.addAttribute(ColumnName.DISPLAY_ORDER, "Wonderful train journey");
        MapperException ex = assertThrows(MapperException.class, () -> {
            item.getBigDecimal(ColumnName.DISPLAY_ORDER);
        });
    }

    @Test
    public void shouldGetInteger() {
        item.addAttribute(ColumnName.DISPLAY_ORDER, "1.0");

        final Integer result = item.getInteger(ColumnName.DISPLAY_ORDER);
        assertThat(result, is(1));
    }

    @Test
    public void shouldFail_whenInvalidInteger() {
        item.addAttribute(ColumnName.DISPLAY_ORDER, "Wonderful train journey");
        MapperException ex = assertThrows(MapperException.class, () -> {
            item.getBigDecimal(ColumnName.DISPLAY_ORDER);
        });
    }

    @Test
    public void shouldGetLocalDate() {
        Date date = new Date();

        item.addAttribute(ColumnName.LIVE_TO, date);
        final LocalDate result = item.getLocalDate(ColumnName.LIVE_TO);

        assertThat(result, is(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
    }

    @Test
    public void shouldFail_whenInvalidLocalDate() {
        item.addAttribute(ColumnName.LIVE_TO, "Wonderful train journey");
        MapperException ex = assertThrows(MapperException.class, () -> {
            item.getLocalDate(ColumnName.LIVE_TO);
        });
    }


    @Test
    public void shouldGetBoolean() {
        assertBooleanValue("yes", TRUE);
        assertBooleanValue("y", TRUE);
        assertBooleanValue("t", TRUE);
        assertBooleanValue("true", TRUE);
        assertBooleanValue("no", FALSE);
        assertBooleanValue("n", FALSE);
        assertBooleanValue("false", FALSE);
        assertBooleanValue("f", FALSE);
    }

    @Test
    public void shouldGetBooleanOrDefault() {
        assertBooleanValueWithDefault("yes", false, TRUE);
        assertBooleanValueWithDefault("no", true, FALSE);
        assertBooleanValueWithDefault(null, true, TRUE);
    }

    @Test
    public void shouldFail_whenInvalidBoolean() {
        item.addAttribute(ColumnName.DEFAULT_HIDDEN, "k");
        MapperException ex = assertThrows(MapperException.class, () -> {
            item.getBoolean(ColumnName.DEFAULT_HIDDEN);
        });
        
        assertThat(ex.getMessage(), containsString(
                "Invalid value 'k' is found in column 'DefaultHidden' in the sheet 'CaseEvent'"));
    }

    @Test
    public void securityClassificationColumnIsPublicLowerCase_called_publicSecurityClassificationEnumValueReturned() {
        SecurityClassificationColumn securityClassificationColumn
            = definitionDataItemWithSecurityClassificationColumnValue("public").getSecurityClassification();

        assertEquals(SecurityClassification.PUBLIC, securityClassificationColumn.getSecurityClassification());
        assertEquals("public", securityClassificationColumn.getColumnValue());
    }

    @Test
    public void securityClassificationColumnIsPublicUpperCase_called_publicSecurityClassificationEnumValueReturned() {
        SecurityClassificationColumn securityClassificationColumn
            = definitionDataItemWithSecurityClassificationColumnValue("PUBLIC").getSecurityClassification();

        assertEquals(SecurityClassification.PUBLIC, securityClassificationColumn.getSecurityClassification());
        assertEquals("PUBLIC", securityClassificationColumn.getColumnValue());
    }

    @Test
    public void securityClassificationColumnIsPublicMixedCase_called_publicSecurityClassificationEnumValueReturned() {
        SecurityClassificationColumn securityClassificationColumn
            = definitionDataItemWithSecurityClassificationColumnValue("pUbLiC").getSecurityClassification();

        assertEquals(SecurityClassification.PUBLIC, securityClassificationColumn.getSecurityClassification());
        assertEquals("pUbLiC", securityClassificationColumn.getColumnValue());
    }

    @SuppressWarnings("checkstyle:LineLength")
    @Test
    public void securityClassificationColumnIsNotAValidEnumConstant_SecurityClassificationColumnReturnedWithNullSecurityClassification() {
        SecurityClassificationColumn securityClassificationColumn
            = definitionDataItemWithSecurityClassificationColumnValue("NotValid").getSecurityClassification();

        assertNull(securityClassificationColumn.getSecurityClassification());
        assertEquals("NotValid", securityClassificationColumn.getColumnValue());
    }

    @Test
    public void displayContextColumnIsOptionalLowerCase_called_OptionalDisplayContextEnumValueReturned() {
        DisplayContextColumn displayContextColumn = definitionDataItemWithDisplayContextColumnValue("optional")
            .getDisplayContext();

        assertEquals(DisplayContext.OPTIONAL, displayContextColumn.getDisplayContext());
        assertEquals("optional", displayContextColumn.getColumnValue());
    }

    @Test
    public void displayContextColumnIsOptionalUpperCase_called_OptionalDisplayContextEnumValueReturned() {
        DisplayContextColumn displayContextColumn = definitionDataItemWithDisplayContextColumnValue("OPTIONAL")
            .getDisplayContext();

        assertEquals(DisplayContext.OPTIONAL, displayContextColumn.getDisplayContext());
        assertEquals("OPTIONAL", displayContextColumn.getColumnValue());
    }

    @Test
    public void displayContextColumnIsOptionalMixedCase_called_OptionalDisplayContextEnumValueReturned() {
        DisplayContextColumn displayContextColumn = definitionDataItemWithDisplayContextColumnValue("oPtiONal")
            .getDisplayContext();

        assertEquals(DisplayContext.OPTIONAL, displayContextColumn.getDisplayContext());
        assertEquals("oPtiONal", displayContextColumn.getColumnValue());
    }

    @Test
    public void displayContextColumnIsNotAValidEnumConstant_displayContextColumnReturnedWithNulldisplayContext() {
        DisplayContextColumn displayContextColumn = definitionDataItemWithDisplayContextColumnValue("NotValid")
            .getDisplayContext();

        assertNull(displayContextColumn.getDisplayContext());
        assertEquals("NotValid", displayContextColumn.getColumnValue());
    }

    @Test
    public void shouldGetCaseFiledId() {
        String caseFieldId = "a_field_id";
        item.addAttribute(ColumnName.CASE_FIELD_ID, caseFieldId);

        assertThat(item.getCaseFieldId(), is(caseFieldId));
    }

    @Test
    public void shouldGetCaseFiledIdFromID() {
        String id = "ID";
        String listElementCode = "listECode";
        item.addAttribute(ColumnName.ID, id);
        item.addAttribute(ColumnName.LIST_ELEMENT_CODE, listElementCode);

        assertThat(item.getCaseFieldId(), is(id + "." + listElementCode));
    }

    private void assertBooleanValue(String field, boolean booleanValue) {
        DefinitionDataItem dataItem = new DefinitionDataItem(SheetName.CASE_EVENT.toString());
        dataItem.addAttribute(ColumnName.DEFAULT_HIDDEN.toString(), field);
        assertThat("asserting " + field, dataItem.getBoolean(ColumnName.DEFAULT_HIDDEN), is(booleanValue));
    }

    private void assertBooleanValueWithDefault(String field, boolean defaultValue, boolean expectedValue) {
        DefinitionDataItem dataItem = new DefinitionDataItem(SheetName.CASE_EVENT.toString());
        dataItem.addAttribute(ColumnName.DEFAULT_HIDDEN.toString(), field);
        assertThat("asserting " + field, dataItem.getBooleanOrDefault(
            ColumnName.DEFAULT_HIDDEN, defaultValue), is(expectedValue));
    }

    private DefinitionDataItem definitionDataItemWithSecurityClassificationColumnValue(
        String securityClassificationColumn) {
        DefinitionDataItem definitionDataItem = new DefinitionDataItem(SheetName.CASE_EVENT.getName());
        definitionDataItem.addAttribute(ColumnName.SECURITY_CLASSIFICATION.toString(), securityClassificationColumn);
        return definitionDataItem;
    }

    private DefinitionDataItem definitionDataItemWithDisplayContextColumnValue(String displayContextColumn) {
        DefinitionDataItem definitionDataItem = new DefinitionDataItem(SheetName.CASE_EVENT_TO_FIELDS.getName());
        definitionDataItem.addAttribute(ColumnName.DISPLAY_CONTEXT.toString(), displayContextColumn);
        return definitionDataItem;
    }
}
