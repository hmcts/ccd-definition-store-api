package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.AuthorisationValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import static uk.gov.hmcts.ccd.definition.store.domain.validation.authorization.CrudValidator.isValidCrud;

@Component
public class CaseTypeEntityCrudValidatorImpl implements CaseTypeEntityValidator {

    @Override
    public ValidationResult validate(final CaseTypeEntity caseType) {
        final ValidationResult validationResult = new ValidationResult();

        for (CaseTypeACLEntity entity : caseType.getCaseTypeACLEntities()) {
            if (!isValidCrud(entity.getCrudAsString())) {
                validationResult.addError(new CaseTypeEntityInvalidCrudValidationError(entity,
                    new AuthorisationValidationContext(caseType)));
            }
        }

        return validationResult;
    }
}
