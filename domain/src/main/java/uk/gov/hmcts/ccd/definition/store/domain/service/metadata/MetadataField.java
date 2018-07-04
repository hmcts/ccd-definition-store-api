package uk.gov.hmcts.ccd.definition.store.domain.service.metadata;

public enum MetadataField {
    JURISDICTION,
    CASE_TYPE,
    STATE,
    CASE_REFERENCE,
    CREATED_DATE,
    LAST_MODIFIED,
    SECURITY_CLASSIFICATION;

    public static MetadataField fromString(String fieldName) {
        for (MetadataField metadataField : values()) {
            if (metadataField.name().equalsIgnoreCase(fieldName)) {
                return metadataField;
            }
        }
        return null;
    }
}
