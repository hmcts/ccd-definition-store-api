package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationCaseFieldValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldUserRoleEntity;

@Component
public class CaseFieldEntityUserRoleValidatorImpl implements CaseFieldEntityValidator {

    @Override
    public ValidationResult validate(final CaseFieldEntity caseField,
                                     final CaseFieldEntityValidationContext caseFieldEntityValidationContext) {

        final ValidationResult validationResult = new ValidationResult();

        for (CaseFieldUserRoleEntity entity : caseField.getCaseFieldUserRoles()) {

            if (null == entity.getUserRole()) {
                validationResult.addError(new CaseFieldEntityInvalidUserRoleValidationError(entity,
                    new AuthorisationCaseFieldValidationContext(caseField, caseFieldEntityValidationContext)));
            }
        }

        return validationResult;
    }
}
