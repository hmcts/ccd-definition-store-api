package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationCaseFieldValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldACLEntity;

import static uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.CrudValidator.isValidCrud;

@Component
public class CaseFieldEntityComplexFieldCrudValidatorImpl implements CaseFieldEntityValidator {

    @Override
    public ValidationResult validate(final CaseFieldEntity caseField,
                                     final CaseFieldEntityValidationContext caseFieldEntityValidationContext) {
        final ValidationResult validationResult = new ValidationResult();

        for (ComplexFieldACLEntity entity : caseField.getComplexFieldACLEntities()) {
            if (!isValidCrud(entity.getCrudAsString())) {
                validationResult.addError(new CaseFieldEntityInvalidComplexCrudValidationError(entity,
                    new AuthorisationCaseFieldValidationContext(caseField, caseFieldEntityValidationContext)));
            }
        }

        return validationResult;
    }
}
