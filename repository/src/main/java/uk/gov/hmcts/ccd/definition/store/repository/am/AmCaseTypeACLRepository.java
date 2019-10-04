package uk.gov.hmcts.ccd.definition.store.repository.am;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface AmCaseTypeACLRepository {

    CaseTypeAmInfo getAmInfoFor(String caseTypeReference);
    List<CaseTypeAmInfo> getAmInfoFor(List<String> caseTypeReferences);
    CaseTypeAmInfo saveAmInfoFor(CaseTypeAmInfo caseTypeAmInfo);
    List<CaseTypeAmInfo> saveAmInfoFor(List<CaseTypeAmInfo> caseTypeAmInfos);
}
