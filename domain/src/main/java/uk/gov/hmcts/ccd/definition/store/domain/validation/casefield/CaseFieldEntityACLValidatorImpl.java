package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationCaseFieldValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

@Component
public class CaseFieldEntityACLValidatorImpl implements CaseFieldEntityValidator {

    @Override
    public ValidationResult validate(final CaseFieldEntity caseField,
                                     final CaseFieldEntityValidationContext caseFieldEntityValidationContext) {

        final ValidationResult validationResult = new ValidationResult();

        for (CaseFieldACLEntity entity : caseField.getCaseFieldACLEntities()) {

            if (null == entity.getUserRole()) {
                validationResult.addError(new CaseFieldEntityInvalidUserRoleValidationError(entity,
                    new AuthorisationCaseFieldValidationContext(caseField, caseFieldEntityValidationContext)));
            }
        }

        return validationResult;
    }
}
