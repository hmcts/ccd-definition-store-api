package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;

import java.util.ArrayList;
import java.util.List;

public class EventCaseFieldEntityValidationContext implements ValidationContext {

    private String eventId;

    private List<EventCaseFieldEntity> allEventCaseFieldEntitiesForEventCase;

    private final List<String> caseRoles;

    public EventCaseFieldEntityValidationContext(String eventId,
                                                 List<EventCaseFieldEntity> allEventCaseFieldEntitiesForEventCase, List<String> caseRoles) {
        this.eventId = eventId;
        this.allEventCaseFieldEntitiesForEventCase = allEventCaseFieldEntitiesForEventCase;
        this.caseRoles = caseRoles;
    }

    public EventCaseFieldEntityValidationContext(String eventId,
                                                 List<EventCaseFieldEntity> allEventCaseFieldEntitiesForEventCase) {
        this(eventId, allEventCaseFieldEntitiesForEventCase, new ArrayList<>());
    }

    public String getEventId() {
        return eventId;
    }

    public List<EventCaseFieldEntity> getAllEventCaseFieldEntitiesForEventCase() {
        return allEventCaseFieldEntitiesForEventCase;
    }

    public List<String> getCaseRoles() {
        return this.caseRoles;
    }
}
