package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.PublishFieldsValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;

@Component
public class EventCaseFieldPublishValidatorImpl implements EventCaseFieldEntityValidator, PublishFieldsValidator {

    @Override
    public ValidationResult validate(EventCaseFieldEntity eventCaseFieldEntity,
                                     EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext) {
        final ValidationResult validationResult = new ValidationResult();
        this.validatePublishAsField(validationResult, eventCaseFieldEntityValidationContext, eventCaseFieldEntity.getPublishAs());
        return validationResult;
    }
}
