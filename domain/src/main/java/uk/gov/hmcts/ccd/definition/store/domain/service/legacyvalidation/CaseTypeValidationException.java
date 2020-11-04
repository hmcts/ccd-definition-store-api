package uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation;

import uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.rules.CaseTypeValidationResult;

import java.util.HashSet;
import java.util.Set;

/**
 * CaseTypeValidationException for runtime expections.
 * Exception thrown when validation of a Case Type fails.
 */
public class CaseTypeValidationException extends RuntimeException {

    private final Set<String> errors;

    public CaseTypeValidationException(CaseTypeValidationResult validationResult) {
        super("Case type validation error");
        this.errors = new HashSet<>();
        this.errors.addAll(validationResult.getErrors());
    }

    /**
     * Get list of errors.
     *
     * @return the list of errors
     */
    public Set<String> getErrors() {
        return this.errors;
    }
}
