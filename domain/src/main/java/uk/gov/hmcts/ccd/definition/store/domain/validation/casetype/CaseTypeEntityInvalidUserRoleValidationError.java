package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeUserRoleEntity;

public class CaseTypeEntityInvalidUserRoleValidationError extends ValidationError {

    private final CaseTypeUserRoleEntity entity;

    private final AuthorisationValidationContext context;

    public CaseTypeEntityInvalidUserRoleValidationError(final CaseTypeUserRoleEntity entity,
                                                 final AuthorisationValidationContext context) {


        super(String.format("Invalid UserRole is not defined for case type '%s'",
            context.getCaseReference()));
        this.entity = entity;
        this.context = context;
    }

    public CaseTypeUserRoleEntity getCaseTypeUserRoleEntity() {
        return entity;
    }

    public AuthorisationValidationContext getAuthorisationValidationContext() {
        return context;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }
}
