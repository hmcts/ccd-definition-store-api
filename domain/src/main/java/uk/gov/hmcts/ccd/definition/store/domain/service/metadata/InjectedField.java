package uk.gov.hmcts.ccd.definition.store.domain.service.metadata;

import java.util.Arrays;

public enum InjectedField {
    INJECTED_DATA;

    InjectedField() {
    }

    public String getReference() {
        return String.join(name(), "[", "");
    }

    public static boolean isInjectedField(String reference) {
        return Arrays.stream(values()).map(InjectedField::getReference).anyMatch(f -> reference.startsWith(f));
    }
}
