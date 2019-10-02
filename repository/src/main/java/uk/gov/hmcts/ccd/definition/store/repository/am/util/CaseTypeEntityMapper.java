package uk.gov.hmcts.ccd.definition.store.repository.am.util;

import uk.gov.hmcts.ccd.definition.store.repository.am.CaseTypeAmInfo;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

public class CaseTypeEntityMapper {
    public static CaseTypeEntity addAmCaseTypeACLDataToCcdCaseTypeEntity(CaseTypeEntity caseTypeEntity,
                                                                         CaseTypeAmInfo caseTypeAmInfo) {
        caseTypeAmInfo.getCaseTypeACLs().forEach(amCaseTypeACLEntity -> {
            caseTypeEntity.getCaseTypeACLEntities().forEach(ccdCaseTypeACLEntity -> {
                if (amCaseTypeACLEntity.getCaseType().getName().equals(ccdCaseTypeACLEntity.getCaseType().getName())
                    && amCaseTypeACLEntity.getUserRole().getName().equals(ccdCaseTypeACLEntity.getUserRole().getName())) {
                    ccdCaseTypeACLEntity.setCreate(amCaseTypeACLEntity.getCreate());
                    ccdCaseTypeACLEntity.setRead(amCaseTypeACLEntity.getRead());
                    ccdCaseTypeACLEntity.setUpdate(amCaseTypeACLEntity.getUpdate());
                    ccdCaseTypeACLEntity.setDelete(amCaseTypeACLEntity.getDelete());
                }
            });
        });

        return caseTypeEntity;
    }
}
