package uk.gov.hmcts.ccd.definition.store.utils;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

public class CaseTypeBuilder {

    private String jurisdictionReference;
    private String caseTypeReference;
    private List<CaseFieldEntity> fields = newArrayList();

    public CaseTypeBuilder() {}

    public CaseTypeBuilder withJurisdiction(String reference) {
        this.jurisdictionReference = reference;
        return this;
    }

    public CaseTypeBuilder withReference(String reference) {
        this.caseTypeReference = reference;
        return this;
    }

    public CaseTypeBuilder withField(CaseFieldEntity field) {
        fields.add(field);
        return this;
    }

    public CaseTypeEntity build() {
        final JurisdictionEntity jurisdiction = new JurisdictionEntity();
        jurisdiction.setReference(this.jurisdictionReference);
        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setJurisdiction(jurisdiction);
        caseType.setReference(this.caseTypeReference);
        caseType.addCaseFields(this.fields);
        return caseType;
    }
}
