package uk.gov.hmcts.ccd.definition.store.repository.am;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateACLEntity;

import java.util.List;

@Data
@Builder
public class CaseFieldAmInfo {
    private SecurityClassification securityClassification;
    private List<CaseFieldACLEntity> caseFieldACLs;
}
