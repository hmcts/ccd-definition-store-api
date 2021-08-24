package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationCaseFieldValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.Authorisation;

public class CaseFieldEntityInvalidAccessProfileValidationError extends ValidationError {

    private final Authorisation caseFieldACLEntity;
    private final AuthorisationCaseFieldValidationContext authorisationCaseFieldValidationContext;

    public CaseFieldEntityInvalidAccessProfileValidationError(final Authorisation entity,
                                                              final AuthorisationCaseFieldValidationContext context) {
        super(String.format("Invalid AccessProfile %s for case type '%s', case field '%s'",
            entity.getAccessProfileId(),
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
