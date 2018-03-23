package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeUserRoleEntity;

public class CaseTypeEntityInvalidCrudValidationError extends ValidationError {

    private final CaseTypeUserRoleEntity entity;
    private final AuthorisationValidationContext context;

    public CaseTypeEntityInvalidCrudValidationError(final CaseTypeUserRoleEntity entity,
                                                    final AuthorisationValidationContext context) {
        super(String.format("Invalid CRUD value '%s' for case type '%s'",
            StringUtils.defaultString(entity.getCrudAsString()),
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
