package uk.gov.hmcts.ccd.definition.store.repository.model;

/**
 * A "lite" version of the {@link CaseState} class that contains selected State fields (id and name) for display
 * purposes.
 */
public class CaseStateLite {
    private String id = null;
    private String name = null;

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
}
