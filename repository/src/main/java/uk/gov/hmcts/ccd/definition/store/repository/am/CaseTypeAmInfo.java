package uk.gov.hmcts.ccd.definition.store.repository.am;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateACLEntity;

import java.util.List;

@Data
@Builder
public class CaseTypeAmInfo {

    private String caseReference;
    private String jurisdictionId;
    private SecurityClassification securityClassification;
    private List<CaseTypeACLEntity> caseTypeACLs;
    private List<EventAMInfo> eventAMInfos;
    private List<StateACLEntity> stateACLs;
    private List<CaseFieldAMInfo> caseFieldAMInfos;

    /*  
    @Valid
    private final ResourceDefinition resourceDefinition;
    @NotBlank
    private final String roleName;
    
    private final Map< JsonPointer,  Entry< Set< Permission>,  SecurityClassification>> attributePermissions;

    @JsonIgnore
    private Instant lastUpdate;
    @Setter
    @JsonIgnore
    private String callingServiceName;

    @JsonIgnore
    @Setter
    private String changedBy;

    @JsonIgnore
    private AuditAction action;*/
}
