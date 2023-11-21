package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.time.LocalDate;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
public class AccessTypeRolesField {

    private Integer id;
    private LocalDate liveFrom;
    private LocalDate liveTo;
    private CaseTypeEntity caseTypeId;
    private String accessTypeId;
    private String organisationProfileId;
    private Boolean accessMandatory;
    private Boolean accessDefault;
    private Boolean display;
    private String description;
    private String hint;
    private Integer displayOrder;
    private String organisationalRoleName;
    private String groupRoleName;
    private String caseAssignedRoleField;
    private Boolean groupAccessEnabled;
    private String caseAccessGroupIdTemplate;

    @JsonIgnore
    public Integer idOfCaseType;
}
