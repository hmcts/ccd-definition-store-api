package uk.gov.hmcts.ccd.definition.store.domain.validation.util;


import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventComplexTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public interface PublishFieldsValidator {

    String PUBLISH_AS_COLUMN = "PublishAs";
    String PUBLISH_COLUMN = "Publish";

    default void validatePublishAsField(ValidationResult validationResult,
                                        EventCaseFieldEntityValidationContext validationContext,
                                        String publishAs, String reference) {

        final String errorDueToSpaces = String.format(PUBLISH_AS_COLUMN
            + " column cannot have spaces, reference '%s'", reference);

        final String errorDuplicatedElements =
            String.format(PUBLISH_AS_COLUMN + " column has an invalid value '%s',  reference '%s'. "
                    + "This value must be unique across CaseEventToFields and EventToComplexTypes for the case type. ",
                publishAs, reference);

        if (publishAs == null || publishAs.isEmpty()) {
            return;
        }
        // Incorrect value for the PublishAs field is defined as a field having spaces (Not permitted)
        if (publishAs.contains(" ")) {
            validationResult.addError(new PublishError(errorDueToSpaces));
        }

        if (isCurrentPublishAsDuplicated(validationContext, publishAs)) {
            validationResult.addError(new PublishError(errorDuplicatedElements));
        }
        return;
    }

    private boolean isCurrentPublishAsDuplicated(
                                                    EventCaseFieldEntityValidationContext validationContext,
                                                    String publishAs
                                                 ) {

        final List<EventCaseFieldEntity> publishesAsInCaseFields =
                    findPublishAsInEventCaseFields(validationContext, publishAs);

        final List<EventComplexTypeEntity> publishesAsInEventComplexTypes =
                    findPublishAsInEventComplexTypes(validationContext, publishAs);

        //case 1 repeated in each TAB  EventCaseField or in EventComplexType
        if (publishesAsInCaseFields.size() > 1 || publishesAsInEventComplexTypes.size() > 1) {
            return true;
        }
        //case 2 same publishAs value in TAB  EventCaseField and in EventComplexType
        if (publishesAsInCaseFields.size() == 1 && publishesAsInEventComplexTypes.size() == 1) {
            return true;
        }
        return false;
    }

    private List<EventComplexTypeEntity> findPublishAsInEventComplexTypes(
        EventCaseFieldEntityValidationContext validationContext,
        String publishAs
    ) {

        return validationContext.getAllEventCaseFieldEntitiesForEventCase().stream()
            .map(eventCaseFieldEntity ->
                eventCaseFieldEntity.getEventComplexTypes().stream().filter(eventComplexTypeEntity ->
                    publishAs.equals(eventComplexTypeEntity.getPublishAs())
                ).collect(Collectors.toList())
            ).flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    private List<EventCaseFieldEntity> findPublishAsInEventCaseFields(
        EventCaseFieldEntityValidationContext validationContext,
        String publishAs
    ) {
        return validationContext.getAllEventCaseFieldEntitiesForEventCase().stream()
            .filter(eventCaseFieldEntity -> publishAs.equals(eventCaseFieldEntity.getPublishAs()))
            .collect(Collectors.toList());
    }

    default void validatePublishField(ValidationResult validationResult,
                                      EventCaseFieldEntityValidationContext validationContext, EventEntity eventEntity,
                                      String reference, Boolean publish) {

        final String errorEventSetToFalse =
            String.format(PUBLISH_COLUMN + " column has an invalid value '%s',  reference '%s'. "
                    + "If the Event is set to false, CaseEventToFields and EventToComplexTypes cannot have "
                    + PUBLISH_COLUMN + " columns as true for the case type.",
                publish, reference);

        if (eventEntity.getPublish() == false && publish == true) {
            validationResult.addError(new PublishError(errorEventSetToFalse));
        }
        return;
    }
}
