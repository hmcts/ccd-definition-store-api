package uk.gov.hmcts.ccd.definition.store.excel.validation;

import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DisplayContextParameterValidatorTest {

    private DisplayContextParameterValidator validator = new DisplayContextParameterValidator();
    private Map<String, DefinitionSheet> definitionSheets;

    @BeforeEach
    public void setup() {
        definitionSheets = new LinkedHashMap<>();
    }

    @Test
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

        InvalidImportException exception = assertThrows(InvalidImportException.class, () -> {
            validator.validate(definitionSheets);
        });
        assertThat(exception.getMessage(), is(
                "Display context parameter #TABLE() has been incorrectly configured or "
                        + "is invalid for field fieldId on tab ComplexTypes"));
    }

    @Test
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

    @Test
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

        InvalidImportException exception = assertThrows(InvalidImportException.class, () -> {
            validator.validate(definitionSheets);
        });
        assertThat(exception.getMessage(), is(
                "Display context parameter #LIST() has been incorrectly configured "
                        + "or is invalid for field fieldId on tab ComplexTypes"));
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
