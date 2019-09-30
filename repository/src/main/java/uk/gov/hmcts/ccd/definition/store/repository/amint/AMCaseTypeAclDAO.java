package uk.gov.hmcts.ccd.definition.store.repository.amint;

import uk.gov.hmcts.ccd.definition.store.repository.AMCaseTypeACLRepository;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeAmInfo;

import java.util.List;

public class AMCaseTypeAclDAO implements AMCaseTypeACLRepository {
    @Override
    public CaseTypeAmInfo getAmInfoFor(String reference) {
        return null;
    }

    @Override
    public List<CaseTypeAmInfo> getAmInfoFor(List<String> caseTypeReference) {
        return null;
    }

    @Override
    public CaseTypeAmInfo saveAmInfoFor(CaseTypeAmInfo caseTypeAmInfo) {
        return null;
    }

    @Override
    public List<CaseTypeAmInfo> saveAmInfoFor(List<CaseTypeAmInfo> caseTypeAmInfos) {
        return null;
    }
}
