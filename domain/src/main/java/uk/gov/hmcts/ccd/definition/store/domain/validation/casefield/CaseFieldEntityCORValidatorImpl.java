package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

@Component
@RequestScope
public class CaseFieldEntityCORValidatorImpl implements CaseFieldEntityValidator {

    @Override
    public ValidationResult validate(CaseFieldEntity caseField,
                                     CaseFieldEntityValidationContext caseFieldEntityValidationContext) {
        final ValidationResult validationResult = new ValidationResult();

        if ((caseField.getFieldType().getReference().equalsIgnoreCase("ChangeOrganisationRequest"))
            && !(caseField.getReference().equalsIgnoreCase("ChangeOrganisationRequest"))) {
            validationResult.addError(new CaseFieldEntityCORValidationError(caseField));

        }
        return validationResult;
    }
}
