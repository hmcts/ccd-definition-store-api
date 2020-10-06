package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationCaseFieldValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldACLEntity;

public class CaseFieldEntityComplexACLValidationError extends ValidationError {

    private final ComplexFieldACLEntity complexFieldACLEntity;
    private final AuthorisationCaseFieldValidationContext authorisationCaseFieldValidationContext;

    public CaseFieldEntityComplexACLValidationError(final ComplexFieldACLEntity entity,
                                                    final AuthorisationCaseFieldValidationContext context) {
        super(String.format(
            "The access for case type '%s', case field '%s', list element code '%s' is more than its parent",
            context.getCaseReference(),
            context.getCaseFieldReference(),
            entity.getListElementCode()));
        this.complexFieldACLEntity = entity;
        this.authorisationCaseFieldValidationContext = context;
    }

    public CaseFieldEntityComplexACLValidationError(final String message, final ComplexFieldACLEntity entity,
                                                    final AuthorisationCaseFieldValidationContext context) {
        super(message);
        this.complexFieldACLEntity = entity;
        this.authorisationCaseFieldValidationContext = context;
    }

    public ComplexFieldACLEntity getComplexFieldACLEntity() {
        return complexFieldACLEntity;
    }


    public AuthorisationCaseFieldValidationContext getAuthorisationCaseFieldValidationContext() {
        return authorisationCaseFieldValidationContext;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }
}
