package uk.gov.hmcts.ccd.definition.store.domain.validation;

import java.util.List;
import java.util.Set;

/**
 * Exception thrown when user roles defined in Case Definition file are missing.
 */
public class MissingAccessProfilesException extends RuntimeException {

    private final Set<String> missingAccessProfiles;
    private final List<ValidationError> validationErrors;

    public MissingAccessProfilesException(Set<String> missingAccessProfiles, List<ValidationError> validationErrors) {
        this.missingAccessProfiles = missingAccessProfiles;
        this.validationErrors = validationErrors;
    }

    public Set<String> getMissingAccessProfiles() {
        return missingAccessProfiles;
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }
}
