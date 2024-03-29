package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "role")
public class AccessTypeRoleResult {

    private String caseTypeId;
    private String organisationalRoleName;
    private String groupRoleName;
    private String caseGroupIdTemplate;
    private boolean groupAccessEnabled;

    @ApiModelProperty(required = true)
    @JsonProperty("caseTypeId")
    public String getCaseTypeId() {
        return caseTypeId;
    }

    public void setCaseTypeId(String caseTypeId) {
        this.caseTypeId = caseTypeId;
    }

    @ApiModelProperty(required = true)
    @JsonProperty("organisationalRoleName")
    public String getOrganisationalRoleName() {
        return organisationalRoleName;
    }

    public void setOrganisationalRoleName(String organisationalRoleName) {
        this.organisationalRoleName = organisationalRoleName;
    }

    @ApiModelProperty(required = true)
    @JsonProperty("groupRoleName")
    public String getGroupRoleName() {
        return groupRoleName;
    }

    public void setGroupRoleName(String groupRoleName) {
        this.groupRoleName = groupRoleName;
    }

    @ApiModelProperty(required = true)
    @JsonProperty("caseGroupIdTemplate")
    public String getCaseGroupIdTemplate() {
        return caseGroupIdTemplate;
    }

    public void setCaseGroupIdTemplate(String caseGroupIdTemplate) {
        this.caseGroupIdTemplate = caseGroupIdTemplate;
    }

    @ApiModelProperty(required = true)
    @JsonProperty("groupAccessEnabled")
    public Boolean getGroupAccessEnabled() {
        return groupAccessEnabled;
    }

    public void setGroupAccessEnabled(Boolean groupAccessEnabled) {
        this.groupAccessEnabled = groupAccessEnabled;
    }

}
