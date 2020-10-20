package uk.gov.hmcts.ccd.definition.store.domain.validation.caserole;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;

@Component
public class CaseRoleEntityFieldValueValidatorImpl implements CaseRoleEntityValidator {

    private static final String CASE_ROLE_ID_REGEX = "^(\\[[A-Za-z]+\\])$";
    private static final int ID_MAX_LENGTH = 255;
    private static final int NAME_MAX_LENGTH = 255;

    @Override
    public ValidationResult validate(CaseRoleEntity caseRoleEntity,
                                     CaseRoleEntityValidationContext caseRoleEntityValidationContext) {
        final ValidationResult validationResult = new ValidationResult();
        if (caseRoleEntity.getReference() == null) {
            validationResult.addError(new CaseRoleEntityFieldValueValidatorImpl.ValidationError(
                String.format("CaseRole ID cannot be null for case type '%s'",
                    caseRoleEntityValidationContext.getCaseName()), caseRoleEntity));
        } else if (!caseRoleEntity.getReference().matches(CASE_ROLE_ID_REGEX)) {
            validationResult.addError(new CaseRoleEntityFieldValueValidatorImpl.ValidationError(
                String.format("CaseRole ID must be only characters with no space and between '[]' for case type '%s'",
                    caseRoleEntityValidationContext.getCaseName()), caseRoleEntity));
        } else if (caseRoleEntity.getReference().length() > ID_MAX_LENGTH) {
            validationResult.addError(new CaseRoleEntityFieldValueValidatorImpl.ValidationError(
                String.format("CaseRole ID must be less than %s characters long for case type '%s'", ID_MAX_LENGTH,
                    caseRoleEntityValidationContext.getCaseName()), caseRoleEntity));
        }
        if (caseRoleEntity.getName() == null) {
            validationResult.addError(new CaseRoleEntityFieldValueValidatorImpl.ValidationError(
                String.format("CaseRole name cannot be null for case type '%s'",
                    caseRoleEntityValidationContext.getCaseName()), caseRoleEntity));
        } else if (caseRoleEntity.getName().length() > NAME_MAX_LENGTH) {
            validationResult.addError(new CaseRoleEntityFieldValueValidatorImpl.ValidationError(
                String.format("CaseRole name must be less than %s characters long for case type '%s'", NAME_MAX_LENGTH,
                    caseRoleEntityValidationContext.getCaseName()), caseRoleEntity));
        } else if (caseRoleEntity.getName().trim().length() < 1) {
            validationResult.addError(new CaseRoleEntityFieldValueValidatorImpl.ValidationError(
                String.format("CaseRole name must be non-empty characters for case type '%s'",
                    caseRoleEntityValidationContext.getCaseName()), caseRoleEntity));
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
