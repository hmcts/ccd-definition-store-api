package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.validation.SpreadsheetValidator;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.DEFAULT_HIDDEN;

@Component
public class SpreadsheetParser {

    private static final Logger logger = LoggerFactory.getLogger(SpreadsheetParser.class);

    private final SpreadsheetValidator spreadsheetValidator;
    private DataFormatter cellFormatter;
    private List<String> importWarnings;

    @Autowired
    public SpreadsheetParser(final SpreadsheetValidator spreadsheetValidator) {
        this.spreadsheetValidator = spreadsheetValidator;
        this.cellFormatter = new DataFormatter();
    }


    public Map<String, DefinitionSheet> parse(InputStream inputStream) throws IOException {
        final Map<String, DefinitionSheet> definitionSheets = new HashMap<>();
        importWarnings = new ArrayList<>();

        try (InputStream is = inputStream) {
            // Java's try-with-resource requires a named local variable, even though unused.
            Workbook workbook = new XSSFWorkbook(inputStream);
            workbook.sheetIterator().forEachRemaining(sheet -> {
                DefinitionSheet definitionSheet = new DefinitionSheet();

                // Keep a list of the attribute column headings; these will be used to populate the key for each
                // attribute.
                List<String> headings = new ArrayList<>();

                //Use a filtered iterator to ignore any rows that are not valid, i.e. ones that have no non-empty cells.
                Iterator<Row> validRowIterator = IteratorUtils.filteredIterator(sheet.iterator(), row -> {
                    Iterator<Cell> cellIterator = row.cellIterator();
                    boolean validRow = false;
                    while (!validRow && cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        if (cell != null
                            && cell.getCellTypeEnum() != CellType.BLANK
                            && !((XSSFCell) cell).getRawValue().isEmpty()) {
                            validRow = true;
                        }
                    }

                    return validRow;
                });

                validRowIterator.forEachRemaining(row -> {
                    if (row.getRowNum() == 0) {
                        // Set the Definition Sheet name from the first cell (A1, usually).
                        definitionSheet.setName(row.getCell(0).getStringCellValue());
                        logger.debug("Processing Definition Sheet: " + definitionSheet.getName());
                    } else if (row.getRowNum() > 1) {
                        // Ignore the second row because it doesn't contain any data to process.
                        if (row.getRowNum() == 2) {
                            // This is the header row. Use a non-filtered iterator because this row defines the
                            // boundary, i.e. all cells in this row are regarded valid.
                            row.iterator().forEachRemaining(cell -> {
                                final String cellValue = cell.getStringCellValue();
                                if (cellValue.equals(DEFAULT_HIDDEN.toString())) {
                                    final String importWarning = definitionSheet.getName()
                                        + " sheet contains DefaultHidden column that will be deprecated. "
                                        + "Please remove from future Definition imports.";
                                    importWarnings.add(importWarning);
                                }
                                headings.add(cell.getStringCellValue());
                            });
                            logger.debug("Number of attribute headers: " + headings.size());
                        } else {
                            // These are data rows. Use a filtered iterator to ignore any cells that fall outside the
                            // boundary defined by the column headers. (If not, this will cause an
                            // ArrayIndexOutOfBoundsException.)
                            Iterator<Cell> validCellIterator = IteratorUtils.filteredIterator(row.iterator(), cell ->
                                cell.getColumnIndex() < headings.size());
                            DefinitionDataItem dataItem = new DefinitionDataItem(definitionSheet.getName());
                            validCellIterator.forEachRemaining(cell -> {
                                final Object cellValue;
                                switch (cell.getCellTypeEnum()) {
                                    case BOOLEAN:
                                        cellValue = cell.getBooleanCellValue();
                                        break;
                                    case NUMERIC:
                                        cellValue = DateUtil.isCellDateFormatted(cell)
                                            ? cell.getDateCellValue() : formatNumberAsString(cell);
                                        break;
                                    case STRING:
                                        cellValue = cell.getStringCellValue();
                                        String columnHeaderName = headings.get(cell.getColumnIndex());
                                        spreadsheetValidator.validate(sheet.getSheetName(), columnHeaderName,
                                            (String) cellValue, cell.getRowIndex() + 1);
                                        break;
                                    default:
                                        cellValue = null;
                                        break;
                                }

                                dataItem.addAttribute(headings.get(cell.getColumnIndex()), cellValue);
                            });
                            definitionSheet.addDataItem(dataItem);
                        }
                    }
                });

                spreadsheetValidator.validate(sheet.getSheetName(), definitionSheet, headings);


                definitionSheets.put(definitionSheet.getName(), definitionSheet);
            });
        }

        return definitionSheets;
    }

    public List<String> getImportWarnings() {
        return importWarnings;
    }

    private String formatNumberAsString(Cell cell) {
        //we need to format a numeric cell as a string to prevent integers from being interpreted as decimals
        return cellFormatter.formatCellValue(cell);
    }
}
