package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationCaseFieldValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.Authorisation;

public class CaseFieldEntityInvalidUserRoleValidationError extends ValidationError {

    private final Authorisation caseFieldACLEntity;
    private final AuthorisationCaseFieldValidationContext authorisationCaseFieldValidationContext;

    public CaseFieldEntityInvalidUserRoleValidationError(final Authorisation entity,
                                                         final AuthorisationCaseFieldValidationContext context) {
        super(String.format("Invalid UserRole %s for case type '%s', case field '%s'",
            entity.getUserRoleId(),
            context.getCaseReference(),
            context.getCaseFieldReference()));
        this.caseFieldACLEntity = entity;
        this.authorisationCaseFieldValidationContext = context;
    }

    public Authorisation getCaseFieldACLEntity() {
        return caseFieldACLEntity;
    }

    public AuthorisationCaseFieldValidationContext getAuthorisationCaseFieldValidationContext() {
        return authorisationCaseFieldValidationContext;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }
}
