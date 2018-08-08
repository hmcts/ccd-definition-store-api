package uk.gov.hmcts.ccd.definition.store.repository.model;

/**
 * A "lite" version of the {@link CaseState} class that contains selected State fields (id, name, and description) for
 * display purposes.
 */
public class CaseStateLite {
    private String id = null;
    private String name = null;
    private String description = null;

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
}
