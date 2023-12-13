package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "role")
public class ATRRoleResult {

    private String caseTypeId;
    private String organisationRoleName;
    private String groupRoleName;
    private String caseGroupIdTemplate;

    @ApiModelProperty(required = true, value = "")
    @JsonProperty("caseTypeId")
    public String getCaseTypeId() {
        return caseTypeId;
    }

    public void setCaseTypeId(String caseTypeId) {
        this.caseTypeId = caseTypeId;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty("organisationRoleName")
    public String getOrganisationRoleName() {
        return organisationRoleName;
    }
    public void setOrganisationRoleName(String organisationRoleName) {
        this.organisationRoleName= organisationRoleName;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty("groupRoleName")
    public String getGroupRoleName() {
        return groupRoleName;
    }

    public void setGroupRoleName(String groupRoleName) {
        this.groupRoleName= groupRoleName;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty("caseGroupIdTemplate")
    public String getCaseGroupIdTemplate() {
        return caseGroupIdTemplate;
    }

    public void setCaseGroupIdTemplate(String caseGroupIdTemplate) {
        this.caseGroupIdTemplate= caseGroupIdTemplate;
    }

}
