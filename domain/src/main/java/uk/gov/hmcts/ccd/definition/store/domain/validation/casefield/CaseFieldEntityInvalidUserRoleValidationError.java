package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationCaseFieldValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldACLEntity;

public class CaseFieldEntityInvalidUserRoleValidationError extends ValidationError {

    private final CaseFieldACLEntity caseFieldACLEntity;
    private final AuthorisationCaseFieldValidationContext authorisationCaseFieldValidationContext;

    public CaseFieldEntityInvalidUserRoleValidationError(final CaseFieldACLEntity entity,
                                                         final AuthorisationCaseFieldValidationContext context) {
        super(String.format("Invalid UserRole for case type '%s', case field '%s'",
            context.getCaseReference(),
            context.getCaseFieldReference()));
        this.caseFieldACLEntity = entity;
        this.authorisationCaseFieldValidationContext = context;
    }

    public CaseFieldACLEntity getCaseFieldACLEntity() {
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
