package uk.gov.hmcts.ccd.definition.store.excel.validation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.ImportValidationException;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.ImportValidationException.Rule;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;

import java.util.List;
import java.util.Map;

@Component
public class SpreadsheetValidator {

    private DisplayContextParameterValidator displayContextParameterValidator = new DisplayContextParameterValidator();

    public void validate(String sheetName, DefinitionSheet definitionSheet, List<String> headings) {
        if (definitionSheet.getName() == null) {
            throw InvalidImportException.missingDefinitionName(sheetName);

        }
        if (headings.isEmpty()) {
            throw new ImportValidationException(ImportValidationException.Reason.MISSING_ATTRIBUTE_HEADERS);
        }
    }

    public void validate(String sheetName, String columnName, String cellValue, String additionalColumnName) {
        SpreadSheetValidationMappingEnum columnNameEnum = SpreadSheetValidationMappingEnum
            .fromSheetColumnName(sheetName, columnName);
        String displayColumnName = additionalColumnName + " - " + columnName;
        validate(sheetName, displayColumnName, columnNameEnum, cellValue, "");
    }

    public void validate(String sheetName, String columnName, String cellValue, Integer rowNumber) {
        SpreadSheetValidationMappingEnum columnNameEnum = SpreadSheetValidationMappingEnum
            .fromSheetColumnName(sheetName, columnName);
        String rowNumberInfo = " at row number '" + rowNumber + "'";
        validate(sheetName, columnName, columnNameEnum, cellValue, rowNumberInfo);
    }

    public void validate(Map<String, DefinitionSheet> definitionSheets) {
        validateOnlyOneJurisdictionPresent(definitionSheets);
        validateAtLeastOnCaseTypePresent(definitionSheets);
        validateCaseFieldWorkSheetIsPresent(definitionSheets);
        validateComplexTypesWorkSheetIsPresent(definitionSheets);
        validateFixedListWorkSheetIsPresent(definitionSheets);
        displayContextParameterValidator.validate(definitionSheets);
    }

    private void validate(String sheetName, String columnName, SpreadSheetValidationMappingEnum columnNameEnum,
                          String cellValue, String rowNumberInfo) {
        if (columnNameEnum != null) {
            Integer columnMaxLength = columnNameEnum.getMaxLength();
            if (columnMaxLength != null && cellValue.length() > columnMaxLength) {
                throw InvalidImportException.columnTooLong(sheetName, columnName, columnMaxLength, rowNumberInfo);
            }
        }
    }

    private void validateOnlyOneJurisdictionPresent(Map<String, DefinitionSheet> sheets) {
        DefinitionSheet jurisdictionSheet = sheets.get(SheetName.JURISDICTION.getName());
        if (jurisdictionSheet == null || jurisdictionSheet.getDataItems().size() != 1) {
            throw ImportValidationException.rule(Rule.JURISDICTION_COUNT);
        }
    }

    private void validateAtLeastOnCaseTypePresent(Map<String, DefinitionSheet> sheets) {
        DefinitionSheet caseTypeSheet = sheets.get(SheetName.CASE_TYPE.getName());
        if (caseTypeSheet == null || caseTypeSheet.getDataItems().isEmpty()) {
            throw ImportValidationException.rule(Rule.MISSING_CASE_TYPE);
        }
    }

    private void validateCaseFieldWorkSheetIsPresent(Map<String, DefinitionSheet> sheets) {
        DefinitionSheet caseFieldSheet = sheets.get(SheetName.CASE_FIELD.getName());
        if (caseFieldSheet == null) {
            throw ImportValidationException.rule(Rule.MISSING_CASE_FIELD_SHEET);
        }
    }

    private void validateComplexTypesWorkSheetIsPresent(Map<String, DefinitionSheet> sheets) {
        DefinitionSheet complexTypesSheet = sheets.get(SheetName.COMPLEX_TYPES.getName());
        if (complexTypesSheet == null) {
            throw ImportValidationException.rule(Rule.MISSING_COMPLEX_TYPES_SHEET);
        }
    }

    private void validateFixedListWorkSheetIsPresent(Map<String, DefinitionSheet> definitionSheets) {
        DefinitionSheet fixedListSheet = definitionSheets.get(SheetName.FIXED_LISTS.getName());
        if (fixedListSheet == null) {
            throw ImportValidationException.rule(Rule.MISSING_FIXED_LIST_SHEET);
        }
    }

    public String getImportValidationFailureMessage(String sheetName, String columnName, Integer columnMaxLength,
                                                    String rowNumberInfo) {
        return String.format(InvalidImportException.COLUMN_LENGTH_MESSAGE, sheetName,
            columnName, columnMaxLength, rowNumberInfo);
    }
}
