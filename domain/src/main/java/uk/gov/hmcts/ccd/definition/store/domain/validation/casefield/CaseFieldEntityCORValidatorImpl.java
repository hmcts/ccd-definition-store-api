package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

import java.util.ArrayList;
import java.util.List;

@Component
@RequestScope
public class CaseFieldEntityCORValidatorImpl implements CaseFieldEntityValidator {

    private final List<String> caseTypesContainingFieldTypeCOR = new ArrayList<>();

    @Override
    public ValidationResult validate(CaseFieldEntity caseField,
                                     CaseFieldEntityValidationContext caseFieldEntityValidationContext) {
        final ValidationResult validationResult = new ValidationResult();

        if (caseField.getFieldType().getReference().equalsIgnoreCase("ChangeOrganisationRequest")) {
            if (caseTypesContainingFieldTypeCOR.contains(caseField.getCaseType().getReference())) {
                validationResult.addError(new CaseFieldEntityCORValidationError(caseField));

            } else {
                caseTypesContainingFieldTypeCOR.add(caseField.getCaseType().getReference());
            }
        }
        return validationResult;
    }
}
