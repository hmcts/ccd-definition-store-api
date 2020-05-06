package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "")
public class CaseTabCollection {

    private String caseTypeId = null;
    private List<String> channels = new ArrayList<>();
    private List<CaseTypeTab> tabs = new ArrayList<>();

    /**
     * Unique identifier for a Case Type.
     **/
    @ApiModelProperty(required = true, value = "Unique identifier for a Case Type.")
    @JsonProperty("case_type_id")
    public String getCaseTypeId() {
        return caseTypeId;
    }

    public void setCaseTypeId(String caseTypeId) {
        this.caseTypeId = caseTypeId;
    }

    /**
     * The channels this tab is targetted at.
     **/
    @ApiModelProperty(value = "The channels this tab is targetted at")
    @JsonProperty("channels")
    public List<String> getChannels() {
        return channels;
    }

    public void setChannels(List<String> channels) {
        this.channels = channels;
    }

    /**
     * returns a lists of tabs.
     **/
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("tabs")
    public List<CaseTypeTab> getTabs() {
        return tabs;
    }

    public void setTabs(List<CaseTypeTab> tabs) {
        this.tabs = tabs;
    }
}
