package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationCaseFieldValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

@Component
public class CaseFieldEntityIdValidatorImpl implements CaseFieldEntityValidator {

    public static final String CASE_FIELD_ID_PATTERN = "^['a-zA-Z0-9\\[\\]\\#%\\&()\\.?_\\£\\s\\xA0-]+$";

    @Override
    public ValidationResult validate(final CaseFieldEntity caseField,
                                     final CaseFieldEntityValidationContext caseFieldEntityValidationContext) {

        final ValidationResult validationResult = new ValidationResult();

        if (!caseField.getReference().matches(CASE_FIELD_ID_PATTERN)) {
            validationResult.addError(new CaseFieldEntityInvalidIdValidationError(caseField,
                new AuthorisationCaseFieldValidationContext(caseField, caseFieldEntityValidationContext)));
        }

        return validationResult;
    }
}
