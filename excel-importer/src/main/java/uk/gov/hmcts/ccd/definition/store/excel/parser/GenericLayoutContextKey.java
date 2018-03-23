package uk.gov.hmcts.ccd.definition.store.excel.parser;

import lombok.Data;

@Data
public class GenericLayoutContextKey {

    private final String caseType;

    private final String caseField;

    private final String label;

    private final String sheet;

    public GenericLayoutContextKey(final String caseType,
                                   final String caseField,
                                   final String label,
                                   final String sheet) {
        this.caseType = caseType;
        this.caseField = caseField;
        this.label = label;
        this.sheet = sheet;
    }
}
