package uk.gov.hmcts.ccd.definition.store.repository.model;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "")
public class JurisdictionUiConfig {

    private String id = null;
    private String name = null;
    private Boolean shuttered = null;

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

    public Boolean getShuttered() {
        return shuttered;
    }

    public void setShuttered(Boolean shuttered) {
        this.shuttered = shuttered;
    }

}
