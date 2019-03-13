package uk.gov.hmcts.ccd.definition.store.repository.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "")
public class FixedListItem implements Serializable {

    private static final long serialVersionUID = -7470053162784618845L;

    private String code = null;
    private String label = null;

    public FixedListItem() {
    }

    public FixedListItem(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     *
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("code")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
