package uk.gov.hmcts.ccd.definition.store.domain.validation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ValidationResult implements Serializable {

    public static final ValidationResult SUCCESS = new ValidationResult();

    private List<ValidationError> validationErrors = new ArrayList<>();

    public ValidationResult() {}

    public ValidationResult(ValidationError error) {
        addError(error);
    }

    public void addError(ValidationError error) {
        this.validationErrors.add(error);
    }

    public void addErrors(List<ValidationError> errors) {
        this.validationErrors.addAll(errors);
    }

    public boolean isValid() {
        return validationErrors.isEmpty();
    }

    public List<ValidationError> getValidationErrors() {
        return this.validationErrors;
    }

    public void merge(ValidationResult validationResult) {
        validationResult.getValidationErrors().forEach(validationError -> {
            if (!hasError(validationError)) {
                this.validationErrors.addAll(validationResult.getValidationErrors());
            }
        });
    }

    private boolean hasError(ValidationError error) {
        return this.validationErrors.stream().anyMatch(validationError -> validationError.equals(error));
    }
}
