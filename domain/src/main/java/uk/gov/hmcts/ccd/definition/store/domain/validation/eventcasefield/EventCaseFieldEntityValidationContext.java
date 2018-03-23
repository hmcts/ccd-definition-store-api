package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;

import java.util.List;

public class EventCaseFieldEntityValidationContext implements ValidationContext {

    private String eventId;

    private List<EventCaseFieldEntity> allEventCaseFieldEntitiesForEventCase;

    public EventCaseFieldEntityValidationContext(String eventId,
                                                 List<EventCaseFieldEntity> allEventCaseFieldEntitiesForEventCase) {
        this.eventId = eventId;
        this.allEventCaseFieldEntitiesForEventCase = allEventCaseFieldEntitiesForEventCase;
    }

    public String getEventId() {
        return eventId;
    }

    public List<EventCaseFieldEntity> getAllEventCaseFieldEntitiesForEventCase() {
        return allEventCaseFieldEntitiesForEventCase;
    }
}
