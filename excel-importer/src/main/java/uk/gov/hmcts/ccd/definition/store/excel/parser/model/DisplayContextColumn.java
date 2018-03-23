package uk.gov.hmcts.ccd.definition.store.excel.parser.model;

import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;

public class DisplayContextColumn {
    private String columnValue;
    private DisplayContext displayContext;

    public DisplayContextColumn(String columnValue, DisplayContext displayContext) {
        this.columnValue = columnValue;
        this.displayContext = displayContext;
    }

    public DisplayContext getDisplayContext() {
        return displayContext;
    }

    public String getColumnValue() {
        return columnValue;
    }

}
