package uk.gov.hmcts.ccd.definition.store.excel.parser.model;

import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;

public class SecurityClassificationColumn {

    private String columnValue;

    private SecurityClassification securityClassification;

    public SecurityClassificationColumn(String columnValue, SecurityClassification securityClassification) {
        this.columnValue = columnValue;
        this.securityClassification = securityClassification;
    }

    public SecurityClassification getSecurityClassification() {
        return securityClassification;
    }

    public String getColumnValue() {
        return columnValue;
    }

}
