package uk.gov.hmcts.ccd.definition.store.utils;

import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchAliasFieldEntity;

import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.newType;

public class SearchAliasFieldBuilder {

    private final String reference;
    private String caseFieldPath;
    private String fieldType;
    private CaseTypeEntity caseType;

    public SearchAliasFieldBuilder(String reference) {
        this.reference = reference;
    }

    public SearchAliasFieldBuilder withCaseFieldPath(String caseFieldPath) {
        this.caseFieldPath = caseFieldPath;
        return this;
    }

    public SearchAliasFieldBuilder withFieldType(String fieldType) {
        this.fieldType = fieldType;
        return this;
    }

    public SearchAliasFieldBuilder withCaseType(CaseTypeEntity caseType) {
        this.caseType = caseType;
        return this;
    }

    public SearchAliasFieldEntity build() {
        SearchAliasFieldEntity searchAliasField = new SearchAliasFieldEntity();
        searchAliasField.setCaseType(caseType);
        searchAliasField.setReference(reference);
        searchAliasField.setCaseFieldPath(caseFieldPath);
        searchAliasField.setFieldType(newType(fieldType).build());

        return searchAliasField;
    }
}
