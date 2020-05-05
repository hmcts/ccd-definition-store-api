package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "")
public class SearchInputField {

    private String caseFieldId = null;
    private String caseFieldElementPath = null;
    private String label = null;
    private Integer order = null;
    private String role;
    private String displayContextParameter;
    private String showCondition;

    @JsonProperty("case_field_id")
    public String getCaseFieldId() {
        return caseFieldId;
    }

    public void setCaseFieldId(String caseFieldId) {
        this.caseFieldId = caseFieldId;
    }

    @JsonProperty("case_field_element_path")
    public String getCaseFieldElementPath() {
        return caseFieldElementPath;
    }

    public void setCaseFieldElementPath(final String caseFieldElementPath) {
        this.caseFieldElementPath = caseFieldElementPath;
    }

    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @JsonProperty("order")
    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    @JsonProperty("role")
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @JsonProperty("show_condition")
    public String getShowCondition() {
        return showCondition;
    }

    public void setShowCondition(String showCondition) {
        this.showCondition = showCondition;
    }


    @JsonProperty("display_context_parameter")
    public String getDisplayContextParameter() {
        return displayContextParameter;
    }

    public void setDisplayContextParameter(String displayContextParameter) {
        this.displayContextParameter = displayContextParameter;
    }
}
