package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.time.LocalDateTime;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class AccessTypeRolesField {

    private Integer id;
    private LocalDateTime liveFrom;
    private LocalDateTime liveTo;
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
    private String organisationPolicy_field;
    private Boolean groupAccessEnabled;
    private String caseAccessGroupIdTemplate;

}
