package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
@Getter
@Setter
public class AccessTypeRoleField implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer id;
    private LocalDate liveFrom;
    private LocalDate liveTo;
    private CaseTypeEntity caseTypeId;
    private String accessTypeId;
    private String organisationProfileId;
    private String organisationalRoleName;
    private String groupRoleName;
    private String caseAssignedRoleField;
    private Boolean groupAccessEnabled;
    private String caseAccessGroupIdTemplate;

}
