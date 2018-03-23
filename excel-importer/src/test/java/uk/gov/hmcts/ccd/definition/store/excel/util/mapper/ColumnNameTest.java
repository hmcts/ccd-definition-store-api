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

    private void assertRequired(SheetName sheetName, List<ColumnName> requiredColumns) {
        for(ColumnName columnName : ColumnName.values()) {
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
