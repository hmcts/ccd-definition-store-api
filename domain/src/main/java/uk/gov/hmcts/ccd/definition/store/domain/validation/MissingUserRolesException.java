package uk.gov.hmcts.ccd.definition.store.domain.validation;

import java.util.List;
import java.util.Set;

/**
 * Exception thrown when user roles defined in Case Definition file are missing.
 */
public class MissingUserRolesException extends RuntimeException {

    private final Set<String> missingUserRoles;
    private final List<ValidationError> validationErrors;

    public MissingUserRolesException(Set<String> missingUserRoles, List<ValidationError> validationErrors) {
        this.missingUserRoles = missingUserRoles;
        this.validationErrors = validationErrors;
    }

    public Set<String> getMissingUserRoles() {
        return missingUserRoles;
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }
}
