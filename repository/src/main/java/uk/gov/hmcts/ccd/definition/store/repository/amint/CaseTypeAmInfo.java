package uk.gov.hmcts.ccd.definition.store.repository.amint;

import java.util.List;

import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateACLEntity;

public class CaseTypeAmInfo {

    private String caseTypeReference;

    private List<CaseTypeACLEntity> caseTypeAcls;

    private List<CaseFieldACLEntity> caseFieldAcls;

    private List<EventACLEntity> eventAcls;

    private List<StateACLEntity> stateAcls;

    // TODO: Security Classifications

}
