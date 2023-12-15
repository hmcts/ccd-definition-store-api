package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;

@ApiModel(description = "accessType")
public class ATRResult {


    private String organisationProfileId;
    private String accessTypeId;
    private Boolean accessMandatory;
    private Boolean accessDefault;
    private Boolean display;
    private String description;
    private String hint;
    private Integer displayOrder;

    private List<ATRRoleResult> roles;

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
    @JsonProperty("accessMandatory")
    public Boolean getAccessMandatory() {
        return accessMandatory;
    }

    public void setAccessMandatory(Boolean accessMandatory) {
        this.accessMandatory = accessMandatory;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty("accessDefault")
    public Boolean getAccessDefault() {
        return accessDefault;
    }

    public void setAccessDefault(Boolean accessDefault) {
        this.accessDefault = accessDefault;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty("display")
    public Boolean getDisplay() {
        return display;
    }

    public void setDisplay(Boolean display) {
        this.display = display;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty("displayOrder")
    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty("hint")
    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty("roles")
    public List<ATRRoleResult> getRoles() {
        return roles;
    }

    public void setRoles(List<ATRRoleResult> roles) {
        this.roles = roles;
    }
}
