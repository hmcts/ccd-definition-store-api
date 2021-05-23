package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeACLEntity;

public class CaseTypeEntityInvalidCrudValidationError extends ValidationError {

    private final CaseTypeACLEntity entity;
    private final AuthorisationValidationContext context;

    public CaseTypeEntityInvalidCrudValidationError(final CaseTypeACLEntity entity,
                                                    final AuthorisationValidationContext context) {
        super(String.format("Invalid CRUD value '%s' for case type '%s'",
            StringUtils.defaultString(entity.getCrudAsString()),
            context.getCaseReference()));
        this.entity = entity;
        this.context = context;
    }

    public CaseTypeACLEntity getCaseTypeUserRoleEntity() {
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
