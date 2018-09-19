package uk.gov.hmcts.ccd.definition.store.domain.validation.caserole;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;

@Component
public class CaseRoleEntityMandatoryFieldsValidatorImpl implements CaseRoleEntityValidator {

    @Override
    public ValidationResult validate(CaseRoleEntity caseRoleEntity,
                                     CaseRoleEntityValidationContext caseRoleEntityValidationContext) {
        final ValidationResult validationResult = new ValidationResult();
        if (caseRoleEntity.getCaseType() == null) {
            String message = String.format("CaseType is null for caseRole '%s'", caseRoleEntity.getReference());
            validationResult.addError(new CaseRoleEntityMandatoryFieldsValidatorImpl.ValidationError(message,
                caseRoleEntity));
        }
        if (caseRoleEntity.getReference() == null) {
            validationResult.addError(new CaseRoleEntityMandatoryFieldsValidatorImpl.ValidationError("CaseRole ID is " +
                "null", caseRoleEntity));
        }
        if (caseRoleEntity.getName() == null) {
            String message = String.format("CaseName is null for caseRole '%s'", caseRoleEntity.getReference());
            validationResult.addError(new CaseRoleEntityMandatoryFieldsValidatorImpl.ValidationError(message,
                caseRoleEntity));
        }
        return validationResult;
    }

    public static class ValidationError extends SimpleValidationError<CaseRoleEntity> {
        public ValidationError(String defaultMessage, CaseRoleEntity entity) {
            super(defaultMessage, entity);
        }

        @Override
        public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
            return validationErrorMessageCreator.createErrorMessage(this);
        }
    }
}
