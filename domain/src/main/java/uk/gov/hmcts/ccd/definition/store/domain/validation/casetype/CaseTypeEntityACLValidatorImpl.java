package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

@Component
public class CaseTypeEntityACLValidatorImpl implements CaseTypeEntityValidator {

    @Override
    public ValidationResult validate(final CaseTypeEntity caseType) {
        final ValidationResult validationResult = new ValidationResult();

        for (CaseTypeACLEntity entity : caseType.getCaseTypeACLEntities()) {

            if (null == entity.getUserRole()) {
                validationResult.addError(new CaseTypeEntityInvalidUserRoleValidationError(entity,
                    new AuthorisationValidationContext(caseType)));
            }
        }

        return validationResult;
    }
}
