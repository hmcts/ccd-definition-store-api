package uk.gov.hmcts.ccd.definition.store.excel.util.mapper;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ColumnNameTest {

    @Test
    public void testRequiredColumnsForJuristication() {

        assertRequired(
            SheetName.JURISDICTION,
            Arrays.asList(ColumnName.NAME, ColumnName.ID)
        );

    }

    @Test
    public void testRequiredColumnsForCaseEventToFields() {

        assertRequired(
            SheetName.CASE_EVENT_TO_FIELDS,
            Arrays.asList(ColumnName.CASE_FIELD_ID, ColumnName.CASE_TYPE_ID,
                ColumnName.CASE_EVENT_ID, ColumnName.PAGE_ID)
        );

    }

    private void assertRequired(SheetName sheetName, List<ColumnName> requiredColumns) {
        for (ColumnName columnName : ColumnName.values()) {
            boolean required
                = requiredColumns.contains(columnName);
            assertEquals(
                String.format(
                    "Expected column %s on %s to be %s", columnName, sheetName,
                    required == true ? "required" : "not required"),
                ColumnName.isRequired(sheetName, columnName),
                required
            );
        }
    }

}
