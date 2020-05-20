package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.*;

import java.util.*;

@ApiModel(description = "")
public class SearchCasesResult {

    private String caseTypeId = null;
    private List<SearchCasesResultField> fields = new ArrayList<>();

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
    public List<SearchCasesResultField> getFields() {
        return fields;
    }

    public void setFields(List<SearchCasesResultField> fields) {
        this.fields = fields;
    }
}
