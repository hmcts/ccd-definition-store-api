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

    @NonNull
    private String caseReference;
    @NonNull
    private String jurisdictionId;
    @NonNull
    private SecurityClassification securityClassification;
    @NonNull
    private List<CaseTypeACLEntity> caseTypeACLs;
    private List<EventAMInfo> eventAMInfos;
    private List<StateACLEntity> stateACLs;
    private List<CaseFieldAMInfo> caseFieldAMInfos;
}
