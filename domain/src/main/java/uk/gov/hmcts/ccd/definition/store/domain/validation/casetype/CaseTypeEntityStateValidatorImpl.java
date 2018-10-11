package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.state.StateEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.domain.validation.state.StateEntityValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;

import java.util.List;

@Component
public class CaseTypeEntityStateValidatorImpl implements CaseTypeEntityValidator {

    private List<StateEntityValidator> stateEntityValidators;

    @Autowired
    public CaseTypeEntityStateValidatorImpl(List<StateEntityValidator> stateEntityValidators) {
        this.stateEntityValidators = stateEntityValidators;
    }

    @Override
    public ValidationResult validate(CaseTypeEntity caseType) {

        ValidationResult validationResult = new ValidationResult();

        for (StateEntityValidator stateEntityValidator : stateEntityValidators) {
            for (StateEntity stateEntity : caseType.getStates()) {
                validationResult.merge(stateEntityValidator.validate(
                    stateEntity,
                    new StateEntityValidationContext(caseType)
                    )
                );
            }
        }
        return validationResult;
    }
}
