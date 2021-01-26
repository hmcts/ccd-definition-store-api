package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.regex.Pattern;

@Component
public class CaseTypeEntityReferenceValidator implements CaseTypeEntityValidator {
    private static final Pattern REFERENCE_PATTERN = Pattern.compile("^[a-zA-Z0-9]+[a-zA-Z_0-9\\-.]*$");
    protected static final String NULL_REFERENCE = "A Case Type must have a reference";
    protected static final String CASE_TYPE_ERROR_MESSAGE = "CaseType can only consist of letters, numbers, "
        + "'_', '.' and '-' characters. Found '%s'";

    @Override
    public ValidationResult validate(CaseTypeEntity caseType) {
        ValidationResult validationResult = new ValidationResult();

        if (caseType.getReference() == null) {
            validationResult.addError(new ReferenceValidationError(NULL_REFERENCE));
        } else {
            if (!REFERENCE_PATTERN.matcher(caseType.getReference()).matches()) {
                validationResult.addError(
                    new ReferenceValidationError(
                        String.format(CASE_TYPE_ERROR_MESSAGE, caseType.getReference()))
                );
            }
        }

        return validationResult;
    }

    public static class ReferenceValidationError extends ValidationError {
        public ReferenceValidationError(String defaultMessage) {
            super(defaultMessage);
        }
    }
}
