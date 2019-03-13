package uk.gov.hmcts.ccd.definition.store.utils;

import java.util.ArrayList;
import java.util.List;

import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchAliasFieldEntity;

public class CaseTypeBuilder {

    private String jurisdictionReference;
    private String jurisdictionDescription;
    private String caseTypeReference;
    private final List<CaseFieldEntity> fields = new ArrayList<>();
    private final List<SearchAliasFieldEntity> searchAliasFields = new ArrayList<>();

    public CaseTypeBuilder withJurisdiction(String reference) {
        this.jurisdictionReference = reference;
        return this;
    }

    public CaseTypeBuilder withJurisdictionDescription(String jurisdictionDescription) {
        this.jurisdictionDescription = jurisdictionDescription;
        return this;
    }

    public CaseTypeBuilder withReference(String reference) {
        this.caseTypeReference = reference;
        return this;
    }

    public CaseTypeBuilder addField(CaseFieldEntity field) {
        fields.add(field);
        return this;
    }

    public CaseTypeBuilder addSearchAliasField(SearchAliasFieldEntity searchAliasField) {
        this.searchAliasFields.add(searchAliasField);
        return this;
    }

    public CaseTypeEntity build() {
        final JurisdictionEntity jurisdiction = new JurisdictionEntity();
        jurisdiction.setReference(this.jurisdictionReference);
        jurisdiction.setDescription(this.jurisdictionDescription);
        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setJurisdiction(jurisdiction);
        caseType.setReference(this.caseTypeReference);
        caseType.addCaseFields(this.fields);
        caseType.addSearchAliasFields(this.searchAliasFields);
        return caseType;
    }
}
