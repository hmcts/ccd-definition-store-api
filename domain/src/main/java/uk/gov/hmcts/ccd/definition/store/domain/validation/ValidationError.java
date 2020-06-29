package uk.gov.hmcts.ccd.definition.store.domain.validation;

import java.io.Serializable;
import java.util.Objects;

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
        return "validationError: " + defaultMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ValidationError)) {
            return false;
        }
        ValidationError that = (ValidationError) o;
        return Objects.equals(defaultMessage, that.defaultMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(defaultMessage);
    }
}
