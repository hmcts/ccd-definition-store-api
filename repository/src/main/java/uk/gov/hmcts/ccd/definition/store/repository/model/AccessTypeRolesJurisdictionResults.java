package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "jurisdictions")
public class AccessTypeRolesJurisdictionResults {

    private List<AccessTypeJurisdictionResult> jurisdictions = new ArrayList<>();

    @JsonProperty("jurisdictions")
    public List<AccessTypeJurisdictionResult> getJurisdictions() {
        return jurisdictions;
    }

    public void setJurisdictions(List<AccessTypeJurisdictionResult> jurisdictions) {
        this.jurisdictions = jurisdictions;
    }
}
