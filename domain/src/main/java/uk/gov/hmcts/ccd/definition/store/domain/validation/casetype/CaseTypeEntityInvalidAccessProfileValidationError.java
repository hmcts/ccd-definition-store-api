package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeACLEntity;

public class CaseTypeEntityInvalidAccessProfileValidationError extends ValidationError {

    private final CaseTypeACLEntity entity;

    private final AuthorisationValidationContext context;

    public CaseTypeEntityInvalidAccessProfileValidationError(final CaseTypeACLEntity entity,
                                                             final AuthorisationValidationContext context) {


        super(String.format("Invalid AccessProfile is not defined for case type '%s'",
            context.getCaseReference()));
        this.entity = entity;
        this.context = context;
    }

    public CaseTypeACLEntity getCaseTypeAccessProfileEntity() {
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
