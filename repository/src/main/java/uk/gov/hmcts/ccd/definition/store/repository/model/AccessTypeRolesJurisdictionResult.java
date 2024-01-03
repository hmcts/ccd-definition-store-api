package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "jurisdiction")
public class AccessTypeRolesJurisdictionResult {

    private String id = null;
    private String name = null;
    private List<AccessTypeRolesResult> accessTypeResults = new ArrayList<AccessTypeRolesResult>();

    /**
     * id of a particular Jurisdiction.
     **/
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("jurisdictionid")
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
    @JsonProperty("jurisdictionName")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("accessTypes")
    public List<AccessTypeRolesResult> getAccessTypeRoles() {
        return accessTypeResults;
    }

    public void setAccessTypeRoles(List<AccessTypeRolesResult> accessTypeResults) {
        this.accessTypeResults = accessTypeResults;
    }
}
