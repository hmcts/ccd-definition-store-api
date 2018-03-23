package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationCaseFieldValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldUserRoleEntity;

public class CaseFieldEntityInvalidUserRoleValidationError extends ValidationError {

    private final CaseFieldUserRoleEntity caseFieldUserRoleEntity;
    private final AuthorisationCaseFieldValidationContext authorisationCaseFieldValidationContext;

    public CaseFieldEntityInvalidUserRoleValidationError(final CaseFieldUserRoleEntity entity,
                                                         final AuthorisationCaseFieldValidationContext context) {
        super(String.format("Invalid UserRole for case type '%s', case field '%s'",
            context.getCaseReference(),
            context.getCaseFieldReference()));
        this.caseFieldUserRoleEntity = entity;
        this.authorisationCaseFieldValidationContext = context;
    }

    public CaseFieldUserRoleEntity getCaseFieldUserRoleEntity() {
        return caseFieldUserRoleEntity;
    }

    public AuthorisationCaseFieldValidationContext getAuthorisationCaseFieldValidationContext() {
        return authorisationCaseFieldValidationContext;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }
}
