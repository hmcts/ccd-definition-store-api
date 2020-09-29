package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationCaseFieldValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

import static uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.CrudValidator.isValidCrud;

@Component
public class CaseFieldEntityCrudValidatorImpl implements CaseFieldEntityValidator {

    @Override
    public ValidationResult validate(final CaseFieldEntity caseField,
                                     final CaseFieldEntityValidationContext caseFieldEntityValidationContext) {
        final ValidationResult validationResult = new ValidationResult();

        for (CaseFieldACLEntity entity : caseField.getCaseFieldACLEntities()) {
            if (!isValidCrud(entity.getCrudAsString())) {
                validationResult.addError(new CaseFieldEntityInvalidCrudValidationError(entity,
                    new AuthorisationCaseFieldValidationContext(caseField, caseFieldEntityValidationContext)));
            }
        }

        return validationResult;
    }
}
