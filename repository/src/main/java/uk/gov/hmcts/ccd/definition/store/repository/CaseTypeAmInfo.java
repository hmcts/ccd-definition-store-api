package uk.gov.hmcts.ccd.definition.store.repository;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateACLEntity;

import java.util.List;

@Data
@Builder
public class CaseTypeAmInfo {

    private String caseReference;
    private List<CaseTypeACLEntity> caseTypeACLs;
    private List<EventACLEntity> eventACLs;
    private List<StateACLEntity> stateACLs;
    private List<CaseFieldACLEntity> caseFieldACLs;
}
