package uk.gov.hmcts.ccd.definition.store.repository.am;

import java.util.List;

public interface AmCaseTypeACLRepository {

    CaseTypeAmInfo getAmInfoFor(String caseTypeReference);
    List<CaseTypeAmInfo> getAmInfoFor(List<String> caseTypeReferences);
    CaseTypeAmInfo saveAmInfoFor(CaseTypeAmInfo caseTypeAmInfo);
    List<CaseTypeAmInfo> saveAmInfoFor(List<CaseTypeAmInfo> caseTypeAmInfos);
}
