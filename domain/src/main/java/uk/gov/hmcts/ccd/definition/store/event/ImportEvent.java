package uk.gov.hmcts.ccd.definition.store.event;

public abstract class ImportEvent<T> {

    private final T caseTypes;

    ImportEvent(T caseTypes) {
        this.caseTypes = caseTypes;
    }

    public T getCaseTypes() {
        return caseTypes;
    }
}
