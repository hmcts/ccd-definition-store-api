package uk.gov.hmcts.ccd.definition.store.repository.model;

import java.util.List;

/**
 * A "lite" version of the {@link CaseType} class that contains selected CaseType fields (id, description, and name) for
 * display purposes.
 */
public class CaseTypeLite {
    private String id = null;
    private String description = null;
    private String name;
    private List<CaseStateLite> states = null;

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

    public List<CaseStateLite> getStates() {
        return states;
    }

    public void setStates(List<CaseStateLite> states) {
        this.states = states;
    }
}
