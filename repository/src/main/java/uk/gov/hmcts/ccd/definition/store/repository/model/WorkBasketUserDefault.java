package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel(description = "")
public class WorkBasketUserDefault implements Serializable {

    private String userIdamId;
    private String workBasketDefaultJurisdiction;
    private String workBasketDefaultCaseType;
    private String workBasketDefaultState;

    @ApiModelProperty(value = "")
    @JsonProperty("id")
    public String getUserIdamId() {
        return userIdamId;
    }

    public void setUserIdamId(String userIdamId) {
        this.userIdamId = userIdamId;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("work_basket_default_jurisdiction")
    public String getWorkBasketDefaultJurisdiction() {
        return workBasketDefaultJurisdiction;
    }

    public void setWorkBasketDefaultJurisdiction(String workBasketDefaultJurisdiction) {
        this.workBasketDefaultJurisdiction = workBasketDefaultJurisdiction;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("work_basket_default_case_type")
    public String getWorkBasketDefaultCaseType() {
        return workBasketDefaultCaseType;
    }

    public void setWorkBasketDefaultCaseType(String workBasketDefaultCaseType) {
        this.workBasketDefaultCaseType = workBasketDefaultCaseType;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("work_basket_default_state")
    public String getWorkBasketDefaultState() {
        return workBasketDefaultState;
    }

    public void setWorkBasketDefaultState(String workBasketDefaultState) {
        this.workBasketDefaultState = workBasketDefaultState;
    }

    @Override
    public String toString() {
        return "WorkBasketUserDefault{"
            + "userIdamId='" + userIdamId + '\''
            + ", workBasketDefaultJurisdiction='" + workBasketDefaultJurisdiction + '\''
            + ", workBasketDefaultCaseType='" + workBasketDefaultCaseType + '\''
            + ", workBasketDefaultState='" + workBasketDefaultState + '\''
            + '}';
    }
}
