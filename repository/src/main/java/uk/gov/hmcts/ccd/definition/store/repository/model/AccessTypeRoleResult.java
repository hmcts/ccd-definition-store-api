package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;

@ApiModel(description = "accessType")
public class AccessTypeRoleResult {

    private String organisationProfileId;
    private String accessTypeId;

    private List<AccessTypeRolesRoleResult> roles;

    @ApiModelProperty(required = true, value = "")
    @JsonProperty("organisationProfileId")
    public String getOrganisationProfileId() {
        return organisationProfileId;
    }

    public void setOrganisationProfileId(String organisationProfileId) {
        this.organisationProfileId = organisationProfileId;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty("accessTypeId")
    public String getAccessTypeId() {
        return accessTypeId;
    }

    public void setAccessTypeId(String accessTypeId) {
        this.accessTypeId = accessTypeId;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty("roles")
    public List<AccessTypeRolesRoleResult> getRoles() {
        return roles;
    }

    public void setRoles(List<AccessTypeRolesRoleResult> roles) {
        this.roles = roles;
    }
}
