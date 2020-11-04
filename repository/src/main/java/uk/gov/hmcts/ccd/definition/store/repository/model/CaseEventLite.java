package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * A "lite" version of the {@link CaseEvent} class that contains selected Event fields (id, name, and description) for
 * display purposes.
 */
public class CaseEventLite implements HasAcls {
    private String id = null;
    private String name = null;
    private String description = null;
    private List<AccessControlList> acls = new ArrayList<>();

    @JsonProperty("pre_states")
    private List<String> preStates = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getPreStates() {
        return preStates;
    }

    public void setPreStates(List<String> preStates) {
        this.preStates = preStates;
    }

    public List<AccessControlList> getAcls() {
        return acls;
    }

    @Override
    public void setAcls(List<AccessControlList> acls) {
        this.acls = acls;
    }
}
