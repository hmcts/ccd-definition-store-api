package uk.gov.hmcts.ccd.definition.store.domain.validation;

import java.io.Serializable;

public class SimpleValidationError<T extends Serializable> extends ValidationError {

    private T entity;

    public SimpleValidationError(String defaultMessage, T entity) {
        super(defaultMessage);
        this.entity = entity;
    }

    public T getEntity() {
        return entity;
    }
}
