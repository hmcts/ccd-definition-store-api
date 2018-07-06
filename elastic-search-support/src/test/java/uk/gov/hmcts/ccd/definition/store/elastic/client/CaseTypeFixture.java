package uk.gov.hmcts.ccd.definition.store.elastic.client;

import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

public class CaseTypeFixture {

    static public CaseTypeEntity newCaseType(String jurisdiction, String caseType) {
        JurisdictionEntity j = new JurisdictionEntity();
        j.setReference(jurisdiction);
        CaseTypeEntity ct = new CaseTypeEntity();
        ct.setReference(caseType);
        ct.setJurisdiction(j);
        return ct;
    }

}
