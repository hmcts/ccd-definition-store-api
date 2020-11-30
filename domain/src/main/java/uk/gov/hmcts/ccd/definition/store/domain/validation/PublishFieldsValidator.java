package uk.gov.hmcts.ccd.definition.store.domain.validation;

import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityValidationContext;

public interface PublishFieldsValidator {

    default void validatePublishAsField(ValidationResult validationResult,
                                        EventCaseFieldEntityValidationContext validationContext, String publishAs) {

        // Incorrect value for the PublishAs field is defined as a field having spaces (Not permitted)
        if (publishAs.contains(" ")){

        }
        validationContext.getAllEventCaseFieldEntitiesForEventCase();
    }
}
