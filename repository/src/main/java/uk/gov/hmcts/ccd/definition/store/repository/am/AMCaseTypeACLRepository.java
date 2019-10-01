package uk.gov.hmcts.ccd.definition.store.repository.am;

import uk.gov.hmcts.ccd.definition.store.repository.am.CaseTypeAmInfo;

import java.util.List;

public interface AMCaseTypeACLRepository {

    CaseTypeAmInfo getAmInfoFor(String reference);
    List<CaseTypeAmInfo> getAmInfoFor(List<String> caseTypeReference);
    CaseTypeAmInfo saveAmInfoFor(CaseTypeAmInfo caseTypeAmInfo);
    List<CaseTypeAmInfo> saveAmInfoFor(List<CaseTypeAmInfo> caseTypeAmInfos);
}
