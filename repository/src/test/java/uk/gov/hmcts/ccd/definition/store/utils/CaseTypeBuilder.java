package uk.gov.hmcts.ccd.definition.store.utils;

import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

public class CaseTypeBuilder {

    private String jurisdictionReference;
    private String caseTypeReference;

    public CaseTypeBuilder() {}

    public CaseTypeBuilder withJurisdiction(String reference) {
        this.jurisdictionReference = reference;
        return this;
    }

    public CaseTypeBuilder withReference(String reference) {
        this.caseTypeReference = reference;
        return this;
    }

    public CaseTypeEntity build() {
        final JurisdictionEntity jurisdiction = new JurisdictionEntity();
        jurisdiction.setReference(this.jurisdictionReference);
        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setJurisdiction(jurisdiction);
        caseType.setReference(this.caseTypeReference);
        return caseType;
    }
}
