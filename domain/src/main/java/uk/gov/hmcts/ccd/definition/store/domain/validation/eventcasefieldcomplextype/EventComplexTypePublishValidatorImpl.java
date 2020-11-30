package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefieldcomplextype;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.PublishFieldsValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventComplexTypeEntity;

@Component
public class EventComplexTypePublishValidatorImpl implements EventComplexTypeEntityValidator, PublishFieldsValidator {

    @Override
    public ValidationResult validate(EventComplexTypeEntity eventCaseFieldEntity,
                                     EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext) {
        final ValidationResult validationResult = new ValidationResult();
        this.validatePublishAsField(validationResult, eventCaseFieldEntityValidationContext, eventCaseFieldEntity.getPublishAs());
        return validationResult;
    }
}
