package uk.gov.hmcts.ccd.definition.store.repository.model;

/**
 * A "lite" version of the {@link CaseType} class that contains selected CaseType fields (description and name) for
 * display purposes. (Class introduced to break a circular dependency between Jurisdiction (containing a list of
 * CaseTypes) and CaseType (containing a reference to a parent Jurisdiction), when using the Mapstruct mapper
 * interface.)
 */
public class CaseTypeLite {
    private String id = null;
    private String description = null;
    private String name;

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
}
