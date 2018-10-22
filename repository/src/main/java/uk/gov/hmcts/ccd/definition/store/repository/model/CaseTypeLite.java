package uk.gov.hmcts.ccd.definition.store.repository.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A "lite" version of the {@link CaseType} class that contains selected CaseType fields (id, description, and name) for
 * display purposes.
 */
public class CaseTypeLite implements HasAcls {
    private String id = null;
    private String description = null;
    private String name;
    private List<CaseState> states = new ArrayList<>();
    private List<CaseEventLite> events = new ArrayList<>();
    private List<AccessControlList> acls = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CaseState> getStates() {
        return states;
    }

    public void setStates(List<CaseState> states) {
        this.states = states;
    }

    public List<CaseEventLite> getEvents() {
        return events;
    }

    public void setEvents(List<CaseEventLite> events) {
        this.events = events;
    }

    public List<AccessControlList> getAcls() {
        return acls;
    }

    @Override
    public void setAcls(List<AccessControlList> acls) {
        this.acls = acls;
    }
}
