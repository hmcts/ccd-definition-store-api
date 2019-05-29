package uk.gov.hmcts.ccd.definition.store.domain.validation;

import java.io.Serializable;

public abstract class ValidationError implements Serializable {

    private final String defaultMessage;

    protected ValidationError(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public final String getDefaultMessage() {
        return defaultMessage;
    }

    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return getDefaultMessage();
    }

    @Override
    public String toString() {
        return "ValidationError{" +
            "defaultMessage='" + defaultMessage + '\'' +
            '}';
    }
}
