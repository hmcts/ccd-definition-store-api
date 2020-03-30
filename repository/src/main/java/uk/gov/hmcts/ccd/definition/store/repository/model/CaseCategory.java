package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "")
public class CaseCategory {

    private String id = null;
    private String label = null;
    private Integer order = null;
    private List<CaseCategory> children = new ArrayList<>();

    @ApiModelProperty(required = true, value = "")
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ApiModelProperty(value = "Label to display.")
    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @ApiModelProperty(value = "Category order.")
    @JsonProperty("order")
    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    @ApiModelProperty(value = "Category children.")
    @JsonProperty("children")
    public List<CaseCategory> getChildren() {
        return children;
    }

    public void setChildren(List<CaseCategory> children) {
        this.children = children;
    }
}
