package uk.gov.hmcts.ccd.definition.store.event;

public abstract class ImportEvent<T> {

    private final T content;

    ImportEvent(T content) {
        this.content = content;
    }

    public T getContent() {
        return content;
    }
}
