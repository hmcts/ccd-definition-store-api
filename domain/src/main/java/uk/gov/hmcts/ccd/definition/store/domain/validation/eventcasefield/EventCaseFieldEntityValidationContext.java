package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EventCaseFieldEntityValidationContext implements ValidationContext {

    private String eventId;

    private List<EventCaseFieldEntity> allEventCaseFieldEntitiesForEventCase;

    private final List<String> caseRoles;

    private Set<String> allDottedComplexFieldPossibilities;

    public EventCaseFieldEntityValidationContext(String eventId,
                                                 List<EventCaseFieldEntity> allEventCaseFieldEntitiesForEventCase,
                                                 List<String> caseRoles) {
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

    public Set<String> getAllDottedComplexFieldPossibilities(CaseFieldEntityUtil caseFieldEntityUtil) {
        if (allDottedComplexFieldPossibilities == null) {
            allDottedComplexFieldPossibilities = caseFieldEntityUtil.buildDottedComplexFieldPossibilities(
                allEventCaseFieldEntitiesForEventCase.stream()
                    .map(EventCaseFieldEntity::getCaseField)
                    .collect(Collectors.toSet()));
        }
        return allDottedComplexFieldPossibilities;
    }
}
