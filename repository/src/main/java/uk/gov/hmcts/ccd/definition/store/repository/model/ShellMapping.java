package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
public class ShellMapping {

    private String shellCaseTypeId;
    private String shellCaseFieldName;
    private String originatingCaseTypeId;
    private String originatingCaseFieldName;
}
