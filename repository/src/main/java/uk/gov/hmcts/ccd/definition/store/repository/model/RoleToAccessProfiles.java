package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Date;
import lombok.Data;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class RoleToAccessProfiles {

    private String caseTypeId;
    private Boolean disabled;
    private Boolean readOnly;
    private String authorisations;
    private String accessProfiles;
    private Date liveFrom;
    private Date liveTo;
    private String roleName;
}
