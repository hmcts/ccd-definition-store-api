package uk.gov.hmcts.ccd.definition.store.excel.validation;

import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SpreadsheetValidatorTest {

    private SpreadsheetValidator validator = new SpreadsheetValidator();
    private Map<String, DefinitionSheet> definitionSheets;

    @BeforeEach
    public void setup() {
        definitionSheets = new LinkedHashMap<>();
        validator = new SpreadsheetValidator();
    }

    @Test
    public void shouldFail_whenCaseTypeIDValueExceedsMaxLength() {
        String columnName = "ID";
        String sheetName = "CaseType";
        int rowNumber = 6;
        InvalidImportException ex = assertThrows(InvalidImportException.class, () -> {
            validator.validate(sheetName, columnName,
                    "TestComplexAddressBookCaseTestComplexAddressBookCaseInvalidExceedingMaxLengthValue", rowNumber);
        });
        String rowNumberInfo = " at row number '" + rowNumber + "'";
        assertThat(ex.getMessage(), is(validator.getImportValidationFailureMessage(
                sheetName, columnName, 70, rowNumberInfo)));

    }

    @Test
    public void shouldPass_withNonExistingColumnName() {
        String columnName = "CRUD";
        int rowNumber = 6;
        validator.validate("sheet", columnName,
                "TestComplexAddressBookCaseTestComplexAddressBookCaseInvalidExceedingMaxLengthValue", rowNumber);

    }

    @Test
    public void shouldPass_whenMaxLengthNotConfigured() {
        String columnName = "nonExistingColumnName";
        int rowNumber = 6;
        try {
            validator.validate("sheet", columnName,
                    "TestComplexAddressBookCaseTestComplexAddressBookCaseInvalidExceedingMaxLengthValue", rowNumber);
        } catch (InvalidImportException ex) {
            assertThat(ex.getMessage(), is(
                    "Error processing sheet \"sheet\": Invalid columnName " + columnName + " at rowNumber "
                    + rowNumber));
            throw ex;
        }
    }

    @Test
    public void shouldFail_whenBlankSheetName() {
        InvalidImportException ex = assertThrows(InvalidImportException.class, () -> {
            validator.validate("sheet", new DefinitionSheet(), Collections.emptyList());
        });
        assertThat(ex.getMessage(), is(
                "Error processing sheet \"sheet\": Invalid Case Definition sheet - "
                + "no Definition name found in Cell A1"));
    }

    @Test
    public void shouldFail_whenEmptyHeader() {
        final DefinitionSheet definitionSheet = new DefinitionSheet();
        definitionSheet.setName("name");

        InvalidImportException ex = assertThrows(InvalidImportException.class, () -> {
            validator.validate("sheet", definitionSheet, Collections.emptyList());
        });
        assertThat(ex.getMessage(), is(
                "Error processing sheet \"sheet\": Invalid Case Definition sheet - "
                + "no Definition data attribute headers found"));
    }

    @Test
    public void shouldValidate_WithHeadings() {
        final DefinitionSheet definitionSheet = new DefinitionSheet();
        definitionSheet.setName("name");

        // This test case ould fail if an exception is thrown; nothing else to assert.
        validator.validate("sheet", definitionSheet, Arrays.asList("N G I T B"));

        // Could have used Latest AssertJ to assert no exception is thrown but it is not in POM
    }

    @Test
    public void shouldFail_whenNoJurisdictioinSheet() {
        MapperException ex = assertThrows(MapperException.class, () -> {
            validator.validate(definitionSheets);
        });
        assertThat(ex.getMessage(), is("A definition must contain exactly one Jurisdiction"));
    }

    @Test
    public void shouldFail_whenNoJurisdictioinDataItems() {

        addDefinitionSheet(SheetName.JURISDICTION);

        MapperException ex = assertThrows(MapperException.class, () -> {
            validator.validate(definitionSheets);
        });
        assertThat(ex.getMessage(), is("A definition must contain exactly one Jurisdiction"));
    }

    @Test
    public void shouldFail_whenNoCaseTypeSheet() {

        final DefinitionSheet sheetJ = addDefinitionSheet(SheetName.JURISDICTION);
        addDataItem(sheetJ);

        MapperException ex = assertThrows(MapperException.class, () -> {
            validator.validate(definitionSheets);
        });
        assertThat(ex.getMessage(), is("A definition must contain at least one Case Type"));
    }

    @Test
    public void shouldFail_whenNoCaseTypeDataItem() {

        final DefinitionSheet sheetJ = addDefinitionSheet(SheetName.JURISDICTION);
        addDataItem(sheetJ);

        addDefinitionSheet(SheetName.CASE_TYPE);

        MapperException ex = assertThrows(MapperException.class, () -> {
            validator.validate(definitionSheets);
        });
        assertThat(ex.getMessage(), is("A definition must contain at least one Case Type"));
    }

    @Test
    public void shouldFail_whenNoCaseFieldSheet() {

        final DefinitionSheet sheetJ = addDefinitionSheet(SheetName.JURISDICTION);
        addDataItem(sheetJ);

        final DefinitionSheet sheetCT = addDefinitionSheet(SheetName.CASE_TYPE);
        addDataItem(sheetCT);

        MapperException ex = assertThrows(MapperException.class, () -> {
            validator.validate(definitionSheets);
        });
        assertThat(ex.getMessage(), is("A definition must contain a Case Field worksheet"));
    }

    @Test
    public void shouldFail_whenNoComplexTypesSheet() {

        final DefinitionSheet sheetJ = addDefinitionSheet(SheetName.JURISDICTION);
        addDataItem(sheetJ);

        final DefinitionSheet sheetCT = addDefinitionSheet(SheetName.CASE_TYPE);
        addDataItem(sheetCT);

        addDefinitionSheet(SheetName.CASE_FIELD);

        MapperException ex = assertThrows(MapperException.class, () -> {
            validator.validate(definitionSheets);
        });
        assertThat(ex.getMessage(), is("A definition must contain a Complex Types worksheet"));
    }

    @Test
    public void shouldFail_whenNoFixedListSheet() {

        final DefinitionSheet sheetJ = addDefinitionSheet(SheetName.JURISDICTION);
        addDataItem(sheetJ);

        final DefinitionSheet sheetCT = addDefinitionSheet(SheetName.CASE_TYPE);
        addDataItem(sheetCT);

        addDefinitionSheet(SheetName.CASE_FIELD);
        addDefinitionSheet(SheetName.COMPLEX_TYPES);

        MapperException ex = assertThrows(MapperException.class, () -> {
            validator.validate(definitionSheets);
        });
        assertThat(ex.getMessage(), is("A definition must contain a Fixed List worksheet"));
    }

    @Test
    public void shouldVaidate_WithAllWorkSheetsInPlace() {

        final DefinitionSheet sheetJ = addDefinitionSheet(SheetName.JURISDICTION);
        addDataItem(sheetJ);

        final DefinitionSheet sheetCT = addDefinitionSheet(SheetName.CASE_TYPE);
        addDataItem(sheetCT);

        final DefinitionSheet sheetCETF = addDefinitionSheet(SheetName.COMPLEX_TYPES);
        DefinitionDataItem definitionDataItem = new DefinitionDataItem(SheetName.COMPLEX_TYPES.getName());
        definitionDataItem.addAttribute(ColumnName.CASE_FIELD_ID, "fieldId");
        sheetCETF.addDataItem(definitionDataItem);

        addDefinitionSheet(SheetName.CASE_FIELD);
        addDefinitionSheet(SheetName.COMPLEX_TYPES);
        addDefinitionSheet(SheetName.FIXED_LISTS);

        // This test case ould fail if an exception is thrown; nothing else to assert.
        validator.validate(definitionSheets);

        // Could have used Latest AssertJ to assert no exception is thrown but it is not in POM
    }

    private DefinitionSheet addDefinitionSheet(SheetName sheetName) {
        final DefinitionSheet sheet = new DefinitionSheet();
        sheet.setName(sheetName.toString());
        definitionSheets.put(sheetName.getName(), sheet);
        return sheet;
    }

    private void addDataItem(final DefinitionSheet sheetCT) {
        sheetCT.addDataItem(new DefinitionDataItem("ngitb"));
    }
}
