package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "")
public class SearchResultDefinition {

    private String caseTypeId = null;
    private List<SearchResultsField> fields = new ArrayList<>();

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
    public List<SearchResultsField> getFields() {
        return fields;
    }

    public void setFields(List<SearchResultsField> fields) {
        this.fields = fields;
    }
}
