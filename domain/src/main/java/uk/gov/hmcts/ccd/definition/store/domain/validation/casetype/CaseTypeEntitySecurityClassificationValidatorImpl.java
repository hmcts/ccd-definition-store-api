package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

@Component
public class CaseTypeEntitySecurityClassificationValidatorImpl implements CaseTypeEntityValidator {

    @Override
    public ValidationResult validate(CaseTypeEntity caseType) {
        ValidationResult validationResult = new ValidationResult();
        if (caseType.getSecurityClassification() == null) {
            validationResult.addError(
                new CaseTypeEntityMissingSecurityClassificationValidationError(caseType)
            );
        }
        return validationResult;
    }

}
