package uk.gov.hmcts.ccd.definition.store.excel.validation;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;

@Component
public class SpreadsheetValidator {

    public void validate(String sheetName, DefinitionSheet definitionSheet, List<String> headings) {
        if (definitionSheet.getName() == null) {
            throw new InvalidImportException("Error processing sheet \"" + sheetName +
                    "\": Invalid Case Definition sheet - no Definition name found in Cell A1");

        }
        if (headings.isEmpty()) {
            throw new InvalidImportException("Error processing sheet \"" + sheetName +
                    "\": Invalid Case Definition sheet - no Definition data attribute headers found");
        }
    }

    public void validate(String sheetName, String columnName, String cellValue, int rowNumber) {
            ColumnName columnNameEnum = ColumnName.fromSheetColumnName(columnName);
            if (columnNameEnum != null) {
                Integer columnMaxLength = columnNameEnum.getMaxLength();
                if (columnMaxLength != null && cellValue.length() > columnMaxLength) {
                    throw new InvalidImportException("Error processing sheet \"" + sheetName +
                                                     "\": Invalid columnName " + columnName + " at rowNumber " + rowNumber);
                }
            }
    }

    public void validate(Map<String, DefinitionSheet> definitionSheets) {
        validateOnlyOneJurisdictionPresent(definitionSheets);
        validateAtLeastOnCaseTypePresent(definitionSheets);
        validateCaseFieldWorkSheetIsPresent(definitionSheets);
        validateComplexTypesWorkSheetIsPresent(definitionSheets);
        validateFixedListWorkSheetIsPresent(definitionSheets);
    }

    private void validateOnlyOneJurisdictionPresent(Map<String, DefinitionSheet> sheets) {
        DefinitionSheet jurisdictionSheet = sheets.get(SheetName.JURISDICTION.getName());
        if (jurisdictionSheet == null || jurisdictionSheet.getDataItems().size() != 1)
            throw new MapperException("A definition must contain exactly one Jurisdiction");
    }

    private void validateAtLeastOnCaseTypePresent(Map<String, DefinitionSheet> sheets) {
        DefinitionSheet caseTypeSheet = sheets.get(SheetName.CASE_TYPE.getName());
        if (caseTypeSheet == null || caseTypeSheet.getDataItems().isEmpty())
            throw new MapperException("A definition must contain at least one Case Type");
    }

    private void validateCaseFieldWorkSheetIsPresent(Map<String, DefinitionSheet> sheets) {
        DefinitionSheet caseFieldSheet = sheets.get(SheetName.CASE_FIELD.getName());
        if (caseFieldSheet == null)
            throw new MapperException("A definition must contain a Case Field worksheet");
    }

    private void validateComplexTypesWorkSheetIsPresent(Map<String, DefinitionSheet> sheets) {
        DefinitionSheet complexTypesSheet = sheets.get(SheetName.COMPLEX_TYPES.getName());
        if (complexTypesSheet == null)
            throw new MapperException("A definition must contain a Complex Types worksheet");
    }

    private void validateFixedListWorkSheetIsPresent(Map<String, DefinitionSheet> definitionSheets) {
        DefinitionSheet fixedListSheet = definitionSheets.get(SheetName.FIXED_LISTS.getName());
        if (fixedListSheet == null)
            throw new MapperException("A definition must contain a Fixed List worksheet");
    }
}
