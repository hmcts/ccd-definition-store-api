package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "")
public class WorkbasketInputDefinition {

    private String caseTypeId = null;
    private List<WorkbasketInputField> fields = new ArrayList<>();

    @ApiModelProperty(value = "")
    @JsonProperty("case_type_id")
    public String getCaseTypeId() {
        return caseTypeId;
    }

    public void setCaseTypeId(String caseTypeId) {
        this.caseTypeId = caseTypeId;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("fields")
    public List<WorkbasketInputField> getFields() {
        return fields;
    }

    public void setFields(List<WorkbasketInputField> fields) {
        this.fields = fields;
    }
}
