package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(description = "")
public class OrganisationProfileIds {

    private List<String> organisationProfileIds = null;

    @ApiModelProperty(value = "")
    @JsonProperty("organisation_profile_ids")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    public List<String> getOrganisationProfileIds() {
        return organisationProfileIds;
    }

    public void setOrganisationProfileIds(List<String> organisationProfileIds) {
        this.organisationProfileIds = organisationProfileIds;
    }
}
