package uk.gov.hmcts.ccd.definition.store.domain.service.metadata;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum MetadataField {
    JURISDICTION,
    CASE_TYPE,
    STATE(true, "State"),
    CASE_REFERENCE,
    CREATED_DATE,
    LAST_MODIFIED_DATE,
    LAST_STATE_MODIFIED_DATE,
    SECURITY_CLASSIFICATION;

    private boolean dynamic;
    private String label;

    MetadataField() {
    }

    MetadataField(boolean dynamic, String label) {
        this.dynamic = dynamic;
        this.label = label;
    }

    private boolean isDynamic() {
        return dynamic;
    }

    public String getLabel() {
        if (label == null) {
            return name();
        }
        return label;
    }

    public String getReference() {
        return String.join(name(), "[", "]");
    }

    public static List<MetadataField> getDynamicFields() {
        return Arrays.stream(values()).filter(MetadataField::isDynamic).collect(Collectors.toList());
    }

    public static boolean isMetadataField(String reference) {
        return Arrays.stream(values()).map(MetadataField::getReference).anyMatch(f -> f.equals(reference));
    }
}
