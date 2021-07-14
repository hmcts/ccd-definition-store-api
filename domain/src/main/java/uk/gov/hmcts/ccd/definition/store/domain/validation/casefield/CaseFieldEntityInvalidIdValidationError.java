package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationCaseFieldValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

import static uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityIdValidatorImpl.CASE_FIELD_ID_PATTERN;

public class CaseFieldEntityInvalidIdValidationError extends ValidationError {

    private final CaseFieldEntity caseFieldEntity;
    private final AuthorisationCaseFieldValidationContext authorisationCaseFieldValidationContext;

    public CaseFieldEntityInvalidIdValidationError(final CaseFieldEntity entity,
                                                   final AuthorisationCaseFieldValidationContext context) {
        super(String.format(
            "case field '%s' for case type '%s' does not match pattern '%s' ",
            entity.getReference(),
            context.getCaseReference(),
            CASE_FIELD_ID_PATTERN
            ));
        this.caseFieldEntity = entity;
        this.authorisationCaseFieldValidationContext = context;
    }

    public CaseFieldEntity getCaseFieldEntity() {
        return caseFieldEntity;
    }

    public AuthorisationCaseFieldValidationContext getAuthorisationCaseFieldValidationContext() {
        return authorisationCaseFieldValidationContext;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }
}
