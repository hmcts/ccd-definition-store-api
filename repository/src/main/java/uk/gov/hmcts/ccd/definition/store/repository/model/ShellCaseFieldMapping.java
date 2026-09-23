package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShellCaseFieldMapping {

    @JsonProperty("OriginatingCaseFieldName")
    private String originatingCaseFieldName;

    @JsonProperty("ShellCaseFieldName")
    private String shellCaseFieldName;
}
