package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShellMappingResponse {

    @JsonProperty("shellCaseTypeID")
    private String shellCaseTypeID;

    @JsonProperty("shellCaseMappings")
    private List<ShellCaseFieldMapping> shellCaseMappings = new ArrayList<>();
}
