package uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.rules;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class CaseTypeValidationResult {

    private Set<String> errors = new HashSet<>();

    public CaseTypeValidationResult() {
        // default constructor
    }

    public CaseTypeValidationResult(String error) {
        this.addError(error);
    }

    /**
     * Add the given error to errors.
     *
     * @param error - error to be added
     */
    public void addError(String error) {
        this.errors.add(error);
    }

    /**
     * Indicate whether validation has passed.
     *
     * @return true if there are no errors, false otherwise
     */
    public boolean validationPassed() {
        return errors.stream().noneMatch(Objects::nonNull);

    }

    /**
     * Get list of error messages.
     *
     * @return the list of errors.
     */
    public Set<String> getErrors() {
        return this.errors;
    }
}
