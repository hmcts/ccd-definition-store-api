package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "")
public class WizardPageField {

    private String caseFieldId = null;
    private Integer order = null;
    private Integer pageColumnNumber;
    private String displayContext = null;
    private List<WizardPageComplexFieldMask> complexFieldMaskList = new ArrayList<>();

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

    @JsonProperty("display_context")
    public String getDisplayContext() {
        return displayContext;
    }

    public void setDisplayContext(DisplayContext displayContext) {
        this.displayContext = displayContext != null ? displayContext.toString() : null;
    }

    @JsonProperty("complex_field_mask")
    public List<WizardPageComplexFieldMask> getComplexFieldMaskList() {
        return complexFieldMaskList;
    }

    public void addComplexFieldMask(WizardPageComplexFieldMask complexFieldMask) {
        this.complexFieldMaskList.add(complexFieldMask);
    }
}
