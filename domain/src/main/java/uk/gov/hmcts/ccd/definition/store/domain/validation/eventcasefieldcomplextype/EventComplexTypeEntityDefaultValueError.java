package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefieldcomplextype;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventComplexTypeEntity;

public class EventComplexTypeEntityDefaultValueError extends ValidationError {

    private final EventCaseFieldEntityValidationContext validationContext;
    private EventComplexTypeEntity eventComplexTypeEntity;

    public EventComplexTypeEntityDefaultValueError(EventComplexTypeEntity eventComplexTypeEntity,
                                                   EventCaseFieldEntityValidationContext validationContext) {
        super(
            String.format(
                "DefaultValue '%s' is not a valid role for '%s' ",
                eventComplexTypeEntity.getDefaultValue(),
                eventComplexTypeEntity.getReference()
            )
        );
        this.eventComplexTypeEntity = eventComplexTypeEntity;
        this.validationContext = validationContext;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }

    public EventComplexTypeEntity getEventCaseFieldEntity() {
        return this.eventComplexTypeEntity;
    }

    public EventCaseFieldEntityValidationContext getValidationContext() {
        return this.validationContext;
    }

}
