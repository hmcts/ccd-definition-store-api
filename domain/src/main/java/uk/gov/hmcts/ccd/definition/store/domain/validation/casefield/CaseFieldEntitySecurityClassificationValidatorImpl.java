package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

@Component
public class CaseFieldEntitySecurityClassificationValidatorImpl implements CaseFieldEntityValidator {


    @Override
    public ValidationResult validate(CaseFieldEntity caseField,
                                     CaseFieldEntityValidationContext caseFieldEntityValidationContext) {

        ValidationResult validationResult = new ValidationResult();

        if (caseField.getSecurityClassification() == null) {
            validationResult.addError(
                new CaseFieldEntityMissingSecurityClassificationValidationError(
                    caseField, caseFieldEntityValidationContext)
            );
            return validationResult;
        }

        SecurityClassification parentSecurityClassification =
            caseFieldEntityValidationContext.getParentSecurityClassification();
        if (parentSecurityClassification != null
            && parentSecurityClassification.isMoreRestrictiveThan(caseField.getSecurityClassification())) {
            validationResult.addError(
                new CaseFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError(
                    caseField, caseFieldEntityValidationContext)
            );
        }

        return validationResult;

    }
}
