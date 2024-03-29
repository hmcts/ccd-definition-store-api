package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.time.LocalDate;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
public class AccessTypeField {

    private Integer id;
    private LocalDate liveFrom;
    private LocalDate liveTo;
    private String caseTypeId;
    private String jurisdictionId;
    private String jurisdictionName;
    private String accessTypeId;
    private String organisationProfileId;
    private Boolean accessMandatory;
    private Boolean accessDefault;
    private Boolean display;
    private String description;
    private String hint;
    private Integer displayOrder;
}
