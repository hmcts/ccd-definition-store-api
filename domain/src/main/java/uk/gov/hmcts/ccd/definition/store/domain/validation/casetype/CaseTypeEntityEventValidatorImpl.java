package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import java.util.List;

@Component
public class CaseTypeEntityEventValidatorImpl implements CaseTypeEntityValidator {

    private List<EventEntityValidator> eventEntityValidators;

    @Autowired
    public CaseTypeEntityEventValidatorImpl(List<EventEntityValidator> eventEntityValidators) {
        this.eventEntityValidators = eventEntityValidators;
    }

    @Override
    public ValidationResult validate(CaseTypeEntity caseType) {

        ValidationResult validationResult = new ValidationResult();

        for (EventEntityValidator eventEntityValidator : eventEntityValidators) {
            for (EventEntity eventEntity : caseType.getEvents()) {
                validationResult.merge(eventEntityValidator.validate(
                    eventEntity,
                    new EventEntityValidationContext(caseType)
                    )
                );
            }
        }

        return validationResult;
    }
}
