package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "jurisdiction")
public class AccessTypeJurisdictionResult {

    private String id = null;
    private String name = null;
    private List<AccessTypeResult> accessTypeResults = new ArrayList<>();

    /**
     * id of a particular Jurisdiction.
     **/
    @ApiModelProperty(required = true)
    @JsonProperty("jurisdictionId")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * name of Jurisdiction.
     **/
    @ApiModelProperty(required = true)
    @JsonProperty("jurisdictionName")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("accessTypes")
    public List<AccessTypeResult> getAccessTypes() {
        return accessTypeResults;
    }

    public void setAccessTypes(List<AccessTypeResult> accessTypeResults) {
        this.accessTypeResults = accessTypeResults;
    }
}
