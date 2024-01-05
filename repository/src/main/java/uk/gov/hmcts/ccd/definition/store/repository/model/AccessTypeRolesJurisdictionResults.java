package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "jurisdictions")
public class AccessTypeRolesJurisdictionResults {

    private List<AccessTypeRolesJurisdictionResult> jurisdictions = new ArrayList<>();

    @ApiModelProperty(value = "")
    @JsonProperty("jurisdictions")
    public List<AccessTypeRolesJurisdictionResult> getJurisdictions() {
        return jurisdictions;
    }

    public void setJurisdictions(List<AccessTypeRolesJurisdictionResult> jurisdictions) {
        this.jurisdictions = jurisdictions;
    }
}
