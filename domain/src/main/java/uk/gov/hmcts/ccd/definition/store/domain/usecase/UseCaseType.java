package uk.gov.hmcts.ccd.definition.store.domain.usecase;

public enum UseCaseType {
    ORGCASES ("ORGCASES"),
    WORKBASKET ("WORKBASKET"),
    SEARCH ("SEARCH");

    private String value;

    UseCaseType(String value) {
        this.value = value;
    }

    public String toString() {
        return this.value;
    }
}
