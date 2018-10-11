package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.List;

@Component
public class CaseTypeEntityCaseFieldsValidatorImpl implements CaseTypeEntityValidator {

    private List<CaseFieldEntityValidator> caseFieldEntityValidators;

    @Autowired
    public CaseTypeEntityCaseFieldsValidatorImpl(List<CaseFieldEntityValidator> caseFieldEntityValidators) {
        this.caseFieldEntityValidators = caseFieldEntityValidators;
    }

    @Override
    public ValidationResult validate(CaseTypeEntity caseType) {

        ValidationResult validationResult = new ValidationResult();

        for (CaseFieldEntityValidator caseFieldEntityValidator : caseFieldEntityValidators) {
            for (CaseFieldEntity caseField : caseType.getCaseFields()) {
                validationResult.merge(
                    caseFieldEntityValidator.validate(
                        caseField,
                        new CaseFieldEntityValidationContext(caseType)
                    )
                );
            }
        }

        return validationResult;
    }
}
