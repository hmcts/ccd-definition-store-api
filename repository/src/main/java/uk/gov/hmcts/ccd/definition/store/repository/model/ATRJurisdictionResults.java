package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "jurisdictions")
public class ATRJurisdictionResults {

    private List<ATRJurisdictionResult> jurisdictions = new ArrayList<ATRJurisdictionResult>();

    @ApiModelProperty(value = "")
    @JsonProperty("jurisdictions")
    public List<ATRJurisdictionResult> getJurisdictions() {
        return jurisdictions;
    }

    public void setJurisdictions(List<ATRJurisdictionResult> jurisdictions) {
        this.jurisdictions = jurisdictions;
    }
}
