package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

import java.util.ArrayList;
import java.util.List;

public class EventCaseFieldEntityValidationContext implements ValidationContext {

    private String eventId;

    private List<EventCaseFieldEntity> allEventCaseFieldEntitiesForEventCase;

    private List<? extends FieldEntity> caseFields;

    private final List<String> caseRoles;

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

    public boolean isDottedComplexFieldPossibility(String path, CaseFieldEntityUtil caseFieldEntityUtil) {
        if (caseFields == null) {
            caseFields = allEventCaseFieldEntitiesForEventCase.stream()
                .map(EventCaseFieldEntity::getCaseField)
                .toList();
        }
        return caseFieldEntityUtil.isDottedComplexFieldPossibility(path, caseFields);
    }
}
