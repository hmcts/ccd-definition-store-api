package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationCaseFieldValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldACLEntity;

import static org.apache.commons.lang3.StringUtils.defaultString;

public class CaseFieldEntityInvalidCrudValidationError extends ValidationError {

    private final AuthorisationCaseFieldValidationContext authorisationCaseFieldValidationContext;
    private final CaseFieldACLEntity caseFieldACLEntity;

    public CaseFieldEntityInvalidCrudValidationError(final CaseFieldACLEntity entity,
                                                     final AuthorisationCaseFieldValidationContext context) {
        super(String.format("Invalid CRUD value '%s' for case type '%s', case field '%s'",
            defaultString(entity.getCrudAsString()),
            context.getCaseReference(),
            context.getCaseFieldReference()));

        this.caseFieldACLEntity = entity;
        this.authorisationCaseFieldValidationContext = context;
    }

    public AuthorisationCaseFieldValidationContext getAuthorisationCaseFieldValidationContext() {
        return authorisationCaseFieldValidationContext;
    }

    public CaseFieldACLEntity getCaseFieldACLEntity() {
        return caseFieldACLEntity;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }
}
