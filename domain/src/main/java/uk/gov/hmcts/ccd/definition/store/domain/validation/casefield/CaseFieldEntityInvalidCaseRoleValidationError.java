package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationCaseFieldValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.Authorisation;

public class CaseFieldEntityInvalidCaseRoleValidationError extends ValidationError {

    private final Authorisation caseFieldACLEntity;
    private final AuthorisationCaseFieldValidationContext authorisationCaseFieldValidationContext;

    public CaseFieldEntityInvalidCaseRoleValidationError(final Authorisation entity,
                                                         final AuthorisationCaseFieldValidationContext context) {
        super(String.format("Unknown case role '%s' for case field '%s'. Please make sure it is declared in the list of supported case roles for the case type '%s'",
            context.getCaseRole(),
            context.getCaseFieldReference(),
            context.getCaseReference()));
        this.caseFieldACLEntity = entity;
        this.authorisationCaseFieldValidationContext = context;
    }

    public Authorisation getCaseFieldACLEntity() {
        return caseFieldACLEntity;
    }

    public AuthorisationCaseFieldValidationContext getAuthCaseFieldEntityInvalidCaseRoleValidationErrororisationCaseFieldValidationContext() {
        return authorisationCaseFieldValidationContext;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }
}
