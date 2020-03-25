package uk.gov.hmcts.ccd.definition.store.excel.validation;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SpreadsheetValidatorTest {

    private SpreadsheetValidator validator = new SpreadsheetValidator();
    private Map<String, DefinitionSheet> definitionSheets;
    private DisplayContextParameterValidator displayContextParameterValidator = new DisplayContextParameterValidator();

    @Before
    public void setup() {
        definitionSheets = new LinkedHashMap<>();
        validator = new SpreadsheetValidator();
    }

    @Test(expected = InvalidImportException.class)
    public void shouldFail_whenCaseTypeIDValueExceedsMaxLength() {
        String columnName = "ID";
        String sheetName = "CaseType";
        int rowNumber = 6;
        try {
            validator.validate(sheetName, columnName,
                "TestComplexAddressBookCaseTestComplexAddressBookCaseInvalidExceedingMaxLengthValue", rowNumber);
        } catch (InvalidImportException ex) {
            String rowNumberInfo = " at row number '" + rowNumber + "'";
            assertThat(ex.getMessage(), is(validator.getImportValidationFailureMessage(sheetName, columnName, 70, rowNumberInfo)));
            throw ex;
        }
    }

    @Test()
    public void shouldPass_withNonExistingColumnName() {
        String columnName = "CRUD";
        int rowNumber = 6;
        try {
            validator.validate("sheet", columnName, "TestComplexAddressBookCaseTestComplexAddressBookCaseInvalidExceedingMaxLengthValue", rowNumber);
        } catch (InvalidImportException ex) {
            assertThat(ex.getMessage(), is(
                "Error processing sheet \"sheet\": Invalid columnName " + columnName + " at rowNumber " + rowNumber));
            throw ex;
        }
    }

    @Test()
    public void shouldPass_whenMaxLengthNotConfigured() {
        String columnName = "nonExistingColumnName";
        int rowNumber = 6;
        try {
            validator.validate("sheet", columnName, "TestComplexAddressBookCaseTestComplexAddressBookCaseInvalidExceedingMaxLengthValue", rowNumber);
        } catch (InvalidImportException ex) {
            assertThat(ex.getMessage(), is(
                "Error processing sheet \"sheet\": Invalid columnName " + columnName + " at rowNumber " + rowNumber));
            throw ex;
        }
    }

    @Test(expected = InvalidImportException.class)
    public void shouldFail_whenBlankSheetName() {
        try {
            validator.validate("sheet", new DefinitionSheet(), Collections.emptyList());
        } catch (InvalidImportException ex) {
            assertThat(ex.getMessage(), is(
                "Error processing sheet \"sheet\": Invalid Case Definition sheet - no Definition name found in Cell A1"));
            throw ex;
        }
    }

    @Test(expected = InvalidImportException.class)
    public void shouldFail_whenEmptyHeader() {
        final DefinitionSheet definitionSheet = new DefinitionSheet();
        definitionSheet.setName("name");

        try {
            validator.validate("sheet", definitionSheet, Collections.emptyList());
        } catch (InvalidImportException ex) {
            assertThat(ex.getMessage(), is(
                "Error processing sheet \"sheet\": Invalid Case Definition sheet - no Definition data attribute headers found"));
            throw ex;
        }
    }

    @Test(expected = Test.None.class)
    public void shouldValidate_WithHeadings() {
        final DefinitionSheet definitionSheet = new DefinitionSheet();
        definitionSheet.setName("name");

        // This test case ould fail if an exception is thrown; nothing else to assert.
        validator.validate("sheet", definitionSheet, Arrays.asList("N G I T B"));

        // Could have used Latest AssertJ to assert no exception is thrown but it is not in POM
    }

    @Test(expected = MapperException.class)
    public void shouldFail_whenNoJurisdictioinSheet() {
        try {
            validator.validate(definitionSheets);
        } catch (MapperException ex) {
            assertThat(ex.getMessage(), is("A definition must contain exactly one Jurisdiction"));
            throw ex;
        }
    }

    @Test(expected = MapperException.class)
    public void shouldFail_whenNoJurisdictioinDataItems() {

        addDefinitionSheet(SheetName.JURISDICTION);

        try {
            validator.validate(definitionSheets);
        } catch (MapperException ex) {
            assertThat(ex.getMessage(), is("A definition must contain exactly one Jurisdiction"));
            throw ex;
        }
    }

    @Test(expected = MapperException.class)
    public void shouldFail_whenNoCaseTypeSheet() {

        final DefinitionSheet sheetJ = addDefinitionSheet(SheetName.JURISDICTION);
        addDataItem(sheetJ);

        try {
            validator.validate(definitionSheets);
        } catch (MapperException ex) {
            assertThat(ex.getMessage(), is("A definition must contain at least one Case Type"));
            throw ex;
        }
    }

    @Test(expected = MapperException.class)
    public void shouldFail_whenNoCaseTypeDataItem() {

        final DefinitionSheet sheetJ = addDefinitionSheet(SheetName.JURISDICTION);
        addDataItem(sheetJ);

        addDefinitionSheet(SheetName.CASE_TYPE);

        try {
            validator.validate(definitionSheets);
        } catch (MapperException ex) {
            assertThat(ex.getMessage(), is("A definition must contain at least one Case Type"));
            throw ex;
        }
    }

    @Test(expected = MapperException.class)
    public void shouldFail_whenNoCaseFieldSheet() {

        final DefinitionSheet sheetJ = addDefinitionSheet(SheetName.JURISDICTION);
        addDataItem(sheetJ);

        final DefinitionSheet sheetCT = addDefinitionSheet(SheetName.CASE_TYPE);
        addDataItem(sheetCT);

        try {
            validator.validate(definitionSheets);
        } catch (MapperException ex) {
            assertThat(ex.getMessage(), is("A definition must contain a Case Field worksheet"));
            throw ex;
        }
    }

    @Test(expected = MapperException.class)
    public void shouldFail_whenNoComplexTypesSheet() {

        final DefinitionSheet sheetJ = addDefinitionSheet(SheetName.JURISDICTION);
        addDataItem(sheetJ);

        final DefinitionSheet sheetCT = addDefinitionSheet(SheetName.CASE_TYPE);
        addDataItem(sheetCT);

        addDefinitionSheet(SheetName.CASE_FIELD);

        try {
            validator.validate(definitionSheets);
        } catch (MapperException ex) {
            assertThat(ex.getMessage(), is("A definition must contain a Complex Types worksheet"));
            throw ex;
        }
    }

    @Test(expected = MapperException.class)
    public void shouldFail_whenNoFixedListSheet() {

        final DefinitionSheet sheetJ = addDefinitionSheet(SheetName.JURISDICTION);
        addDataItem(sheetJ);

        final DefinitionSheet sheetCT = addDefinitionSheet(SheetName.CASE_TYPE);
        addDataItem(sheetCT);

        addDefinitionSheet(SheetName.CASE_FIELD);
        addDefinitionSheet(SheetName.COMPLEX_TYPES);

        try {
            validator.validate(definitionSheets);
        } catch (MapperException ex) {
            assertThat(ex.getMessage(), is("A definition must contain a Fixed List worksheet"));
            throw ex;
        }
    }

    @Test(expected = Test.None.class)
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
