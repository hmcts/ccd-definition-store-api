package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.Date;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class AccessTypeRoles {

    private Date liveFrom;
    private Date liveTo;
    private String caseTypeId;
    private String accessTypeID;
    private String organisationProfileID;
    private Boolean accessMandatory;
    private Boolean accessDefault;
    private Boolean display;
    private String description;
    private String hint;
    private Integer displayOrder;
    private String organisationalRoleName;
    private String groupRoleName;
    private String organisationPolicyField;
    private Boolean groupAccessEnabled;
    private String caseGroupIdTemplate;
}
