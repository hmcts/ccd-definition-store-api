package uk.gov.hmcts.ccd.definition.store.domain.validation.searchaliasfield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationContext;

public class SearchAliasFieldValidationContext implements ValidationContext {

    private static final long serialVersionUID = 6023823136924737081L;

    private final String caseType;
    private final String caseField;

    public SearchAliasFieldValidationContext(String caseType, String caseField) {
        this.caseType = caseType;
        this.caseField = caseField;
    }

    public String getCaseType() {
        return caseType;
    }

    public String getCaseField() {
        return caseField;
    }
}
