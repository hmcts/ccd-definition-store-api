package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "")
public class CaseState {

    private String id = null;
    private String name = null;
    private String description = null;
    private Integer order = null;
    private String titleDisplay;
    private List<AccessControlList> acls = new ArrayList<>();

    /**
     * id of the a particular state.
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
    @ApiModelProperty(value = "Short name to display.")
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * description associated with a state.
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("order")
    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    @ApiModelProperty(value = "Title label to be displayed for state")
    @JsonProperty("title_display")
    public String getTitleDisplay() {
        return titleDisplay;
    }

    public void setTitleDisplay(String titleDisplay) {
        this.titleDisplay = titleDisplay;
    }

    @ApiModelProperty(value = "State Access Control Lists")
    @JsonProperty("acls")
    public List<AccessControlList> getAcls() {
        return acls;
    }

    public void setAcls(List<AccessControlList> acls) {
        this.acls = acls;
    }

}
