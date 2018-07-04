package uk.gov.hmcts.ccd.definition.store.elastic.mapping;

public class FieldMapping {
    private String fieldName;
    private String mapping;

    FieldMapping(String fieldName, String mapping) {
        this.fieldName = fieldName;
        this.mapping = mapping;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getMapping() {
        return mapping;
    }
}
