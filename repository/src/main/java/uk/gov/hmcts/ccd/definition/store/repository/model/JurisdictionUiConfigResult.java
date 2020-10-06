package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(description = "")
public class JurisdictionUiConfigResult {

    private List<JurisdictionUiConfig> configs;

    public JurisdictionUiConfigResult(List<JurisdictionUiConfig> configs) {
        this.configs = configs;
    }

    /**
     * returns a list of Jurisdiction UI config.
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("configs")
    public List<JurisdictionUiConfig> getConfigs() {
        return configs;
    }

    public void setConfigs(List<JurisdictionUiConfig> configs) {
        this.configs = configs;
    }

}
