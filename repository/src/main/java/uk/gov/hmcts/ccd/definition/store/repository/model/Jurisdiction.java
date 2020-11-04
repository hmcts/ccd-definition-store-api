package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApiModel(description = "")
public class Jurisdiction {

    private String id = null;
    private String name = null;
    private String description = null;
    private Date liveFrom = null;
    private Date liveUntil = null;

    // Use the "lite" version of CaseType, since we do not require the full entity mapping.
    private List<CaseTypeLite> caseTypes = new ArrayList<>();

    /**
     * id of a particular Jurisdiction.
     **/
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * name of Jurisdiction.
     **/
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * description of a particular Jurisdiction.
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * date a Jurisdiction went live.
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("live_from")
    public Date getLiveFrom() {
        return liveFrom;
    }

    public void setLiveFrom(Date liveFrom) {
        this.liveFrom = liveFrom;
    }

    /**
     * date upon which a Jurisdiction should no longer be active.
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("live_until")
    public Date getLiveUntil() {
        return liveUntil;
    }

    public void setLiveUntil(Date liveUntil) {
        this.liveUntil = liveUntil;
    }

    /**
     * return a list of case types associated with a given Jurisdiction.
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("case_types")
    public List<CaseTypeLite> getCaseTypes() {
        return caseTypes;
    }

    public void setCaseTypes(List<CaseTypeLite> caseTypes) {
        this.caseTypes = caseTypes;
    }
}
