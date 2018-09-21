package uk.gov.hmcts.ccd.definition.store.domain.validation.caserole;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;

@Component
public class CaseRoleEntityUniquenessValidatorImpl implements CaseRoleEntityValidator {

    private static final String CREATOR = "[CREATOR]";

    @Override
    public ValidationResult validate(CaseRoleEntity caseRoleEntity,
                                     CaseRoleEntityValidationContext caseRoleEntityValidationContext) {
        final ValidationResult validationResult = new ValidationResult();
        if (CREATOR.equalsIgnoreCase(caseRoleEntity.getReference())) {
            validationResult.addError(new CaseRoleEntityUniquenessValidatorImpl.ValidationError(String.format(
                "CaseRole Id [CREATOR] is reserved. Please check case type'%s'",
                caseRoleEntity.getReference()),
                caseRoleEntity));
        } else if (caseRoleEntity.getReference() != null) {
            final List<CaseRoleEntity> caseRoleEntities = caseRoleEntityValidationContext.getCaseRoleEntities();
            final Optional<CaseRoleEntity> duplicateEntity =
                caseRoleEntities
                    .stream()
                    .filter(re -> !caseRoleEntity.equals(re) && re.getReference().equalsIgnoreCase(caseRoleEntity.getReference()))
                    .findFirst();
            duplicateEntity.ifPresent(caseRoleEntity1 ->
                validationResult.addError(new CaseRoleEntityUniquenessValidatorImpl.ValidationError(String.format(
                    "CaseRole with Id '%s' is duplicate. Please check case type'%s'", caseRoleEntity.getReference(),
                    caseRoleEntityValidationContext.getCaseName()),
                    caseRoleEntity))
            );
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
