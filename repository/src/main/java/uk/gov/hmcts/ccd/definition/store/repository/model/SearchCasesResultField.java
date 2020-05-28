package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.*;

import java.util.*;

@ApiModel(description = "")
public class SearchCasesResultField {

    private String caseFieldId = null;
    private String caseFieldElementPath = null;
    private String label = null;
    private Integer order = null;
    private boolean metadata;
    private String role;
    private String useCase;
    private SortOrder sortOrder;
    private String displayContextParameter;

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

    public boolean isMetadata() {
        return metadata;
    }

    public void setMetadata(boolean metadata) {
        this.metadata = metadata;
    }

    @JsonProperty("role")
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @JsonProperty("use_case")
    public String getUseCase() {
        return useCase;
    }

    public void setUseCase(String useCase) {
        this.useCase = useCase;
    }

    @JsonProperty("sort_order")
    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    @JsonProperty("display_context_parameter")
    public String getDisplayContextParameter() {
        return displayContextParameter;
    }

    public void setDisplayContextParameter(String displayContextParameter) {
        this.displayContextParameter = displayContextParameter;
    }
}
