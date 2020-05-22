package uk.gov.hmcts.ccd.definition.store.excel.validation;

import org.junit.*;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.*;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.*;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.*;

import java.util.*;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DisplayContextParameterValidatorTest {

    private DisplayContextParameterValidator validator = new DisplayContextParameterValidator();
    private Map<String, DefinitionSheet> definitionSheets;

    @Before
    public void setup() {
        definitionSheets = new LinkedHashMap<>();
    }

    @Test(expected = InvalidImportException.class)
    public void shouldFail_whenDisplayContextParameterHasTableInCaseEventToFields() {

        final DefinitionSheet sheetJ = addDefinitionSheet(SheetName.JURISDICTION);
        addDataItem(sheetJ);

        final DefinitionSheet sheetCT = addDefinitionSheet(SheetName.CASE_TYPE);
        addDataItem(sheetCT);

        final DefinitionSheet sheetCETF = addDefinitionSheet(SheetName.COMPLEX_TYPES);
        DefinitionDataItem definitionDataItem = new DefinitionDataItem(SheetName.COMPLEX_TYPES.getName());
        definitionDataItem.addAttribute(ColumnName.DISPLAY_CONTEXT_PARAMETER, "#TABLE()");
        definitionDataItem.addAttribute(ColumnName.CASE_FIELD_ID, "fieldId");
        sheetCETF.addDataItem(definitionDataItem);

        addDefinitionSheet(SheetName.CASE_FIELD);
        addDefinitionSheet(SheetName.FIXED_LISTS);

        try {
            validator.validate(definitionSheets);
        } catch (InvalidImportException ex) {
            assertThat(ex.getMessage(), is("Display context parameter #TABLE() has been incorrectly configured or is invalid for field fieldId on tab ComplexTypes"));
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
        definitionDataItem.addAttribute(ColumnName.DISPLAY_CONTEXT_PARAMETER, "#DATETIMEENTRY()");
        definitionDataItem.addAttribute(ColumnName.CASE_FIELD_ID, "fieldId");
        sheetCETF.addDataItem(definitionDataItem);

        addDefinitionSheet(SheetName.CASE_FIELD);
        addDefinitionSheet(SheetName.COMPLEX_TYPES);
        addDefinitionSheet(SheetName.FIXED_LISTS);

        validator.validate(definitionSheets);

    }


    @Test(expected = InvalidImportException.class)
    public void shouldFail_whenDisplayContextParameterHasListInCaseEventToFields() {

        final DefinitionSheet sheetJ = addDefinitionSheet(SheetName.JURISDICTION);
        addDataItem(sheetJ);

        final DefinitionSheet sheetCT = addDefinitionSheet(SheetName.CASE_TYPE);
        addDataItem(sheetCT);

        final DefinitionSheet sheetCETF = addDefinitionSheet(SheetName.COMPLEX_TYPES);
        DefinitionDataItem definitionDataItem = new DefinitionDataItem(SheetName.COMPLEX_TYPES.getName());
        definitionDataItem.addAttribute(ColumnName.DISPLAY_CONTEXT_PARAMETER, "#LIST()");
        definitionDataItem.addAttribute(ColumnName.CASE_FIELD_ID, "fieldId");
        sheetCETF.addDataItem(definitionDataItem);

        addDefinitionSheet(SheetName.CASE_FIELD);
        addDefinitionSheet(SheetName.FIXED_LISTS);

        try {
            validator.validate(definitionSheets);
        } catch (InvalidImportException ex) {
            assertThat(ex.getMessage(), is("Display context parameter #LIST() has been incorrectly configured or is invalid for field fieldId on tab ComplexTypes"));
            throw ex;
        }
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
