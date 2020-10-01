package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationCaseFieldValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldACLEntity;

import static org.apache.commons.lang3.StringUtils.defaultString;

public class CaseFieldEntityInvalidComplexCrudValidationError extends ValidationError {

    private final AuthorisationCaseFieldValidationContext authorisationCaseFieldValidationContext;
    private final ComplexFieldACLEntity complexFieldACLEntity;

    public CaseFieldEntityInvalidComplexCrudValidationError(final ComplexFieldACLEntity entity,
                                                            final AuthorisationCaseFieldValidationContext context) {
        super(String.format("Invalid CRUD value '%s' for case type '%s', case field '%s', list element code '%s'",
            defaultString(entity.getCrudAsString()),
            context.getCaseReference(),
            context.getCaseFieldReference(),
            entity.getListElementCode()));

        this.complexFieldACLEntity = entity;
        this.authorisationCaseFieldValidationContext = context;
    }

    public AuthorisationCaseFieldValidationContext getAuthorisationCaseFieldValidationContext() {
        return authorisationCaseFieldValidationContext;
    }

    public ComplexFieldACLEntity getComplexFieldACLEntity() {
        return complexFieldACLEntity;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }
}
