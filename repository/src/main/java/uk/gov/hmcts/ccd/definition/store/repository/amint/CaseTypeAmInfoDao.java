package uk.gov.hmcts.ccd.definition.store.repository.amint;

import java.util.List;

public interface CaseTypeAmInfoDao {

    CaseTypeAmInfo getAmInfoOf(String caseTypeReference);

    List<CaseTypeAmInfo> getAmInfoOf(List<String> caseTypeReferences);

    void saveAmInfoOf(CaseTypeAmInfo caseTypeAmInfo);

    void saveAmInfoFor(List<CaseTypeAmInfo> caseTypeAmInfo);

}
