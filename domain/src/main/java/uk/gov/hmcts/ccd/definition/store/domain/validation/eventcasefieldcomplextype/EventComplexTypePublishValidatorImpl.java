package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefieldcomplextype;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.domain.validation.util.PublishFieldsValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventComplexTypeEntity;

@Component
public class EventComplexTypePublishValidatorImpl implements EventComplexTypeEntityValidator, PublishFieldsValidator {

    @Override
    public ValidationResult validate(EventComplexTypeEntity eventComplexTypeEntity,
                                     EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext) {

        final String reference = eventComplexTypeEntity.getComplexFieldType().getCaseField().getReference()
            + "." + eventComplexTypeEntity.getReference();
        final ValidationResult validationResult = new ValidationResult();

        this.validatePublishAsField(
            validationResult,
            eventCaseFieldEntityValidationContext,
            eventComplexTypeEntity.getPublishAs(),
            reference
        );

        this.validatePublishField(
            validationResult,
            eventCaseFieldEntityValidationContext,
            eventComplexTypeEntity.getComplexFieldType().getEvent(),
            reference,
            eventComplexTypeEntity.getPublish()
        );
        return validationResult;
    }
}
