package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

@Component
public class EventEntityEnablingConditionValidator extends AbstractShowConditionValidator {

    @Autowired
    public EventEntityEnablingConditionValidator(final ShowConditionParser showConditionExtractor,
                                                 final CaseFieldEntityUtil caseFieldEntityUtil) {
        super(showConditionExtractor, caseFieldEntityUtil);
    }

    @Override
    public ValidationResult validate(final EventEntity eventEntity,
                                     final EventEntityValidationContext eventEntityValidationContext) {

        final ValidationResult validationResult = new ValidationResult();
        validateShowConditionFields(eventEntity, validationResult, eventEntity.getEventEnablingCondition());
        return validationResult;
    }

    public ValidationError getValidationError(String showConditionField,
                                              EventEntity eventEntity,
                                              String showCondition) {
        return new EventEntityEnableConditionReferencesInvalidCaseFieldError(showConditionField,
            eventEntity,
            showCondition);
    }
}
