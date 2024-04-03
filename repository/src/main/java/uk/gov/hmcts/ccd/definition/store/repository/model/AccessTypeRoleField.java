package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.time.LocalDate;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
public class AccessTypeRoleField {

    private Integer id;
    private LocalDate liveFrom;
    private LocalDate liveTo;
    private String caseTypeId;
    private String accessTypeId;
    private String organisationProfileId;
    private String organisationalRoleName;
    private String groupRoleName;
    private String caseAssignedRoleField;
    private Boolean groupAccessEnabled;
    private String caseAccessGroupIdTemplate;

}
