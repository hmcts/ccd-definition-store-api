package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "")
public class CaseCategoryGroup {

    private String id = null;
    private String name = null;
    private Integer order = null;
    private List<CaseCategory> categories = new ArrayList<>();

    /**
     **/
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Short name to display.
     **/
    @ApiModelProperty(value = "Category Group name to display.")
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ApiModelProperty(value = "Category Group order.")
    @JsonProperty("order")
    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    @ApiModelProperty(value = "Group Categories")
    @JsonProperty("categories")
    public List<CaseCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<CaseCategory> categories) {
        this.categories = categories;
    }

}
