package uk.gov.hmcts.ccd.definition.store.repository.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "")
public class WizardPageField {

    private String caseFieldId = null;
    private Integer order = null;
    private Integer pageColumnNumber;
    private List<WizardPageComplexFieldOverride> complexFieldOverrides = new ArrayList<>();

    @JsonProperty("case_field_id")
    public String getCaseFieldId() {
        return caseFieldId;
    }

    public void setCaseFieldId(String caseFieldId) {
        this.caseFieldId = caseFieldId;
    }

    @JsonProperty("order")
    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    @JsonProperty("page_column_no")
    public Integer getPageColumnNumber() {
        return pageColumnNumber;
    }

    public void setPageColumnNumber(Integer number) {
        this.pageColumnNumber = number;
    }

    @JsonProperty("complex_field_overrides")
    public List<WizardPageComplexFieldOverride> getComplexFieldOverrides() {
        return complexFieldOverrides;
    }

    public void addAllComplexFieldOverrides(List<WizardPageComplexFieldOverride> complexFieldOverrides) {
        this.complexFieldOverrides.addAll(complexFieldOverrides);
    }
}
