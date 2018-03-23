package uk.gov.hmcts.ccd.definition.store.domain.validation.authorization;

import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationContext;

public class AuthorisationValidationContext implements ValidationContext {

    private final String caseReference;

    public AuthorisationValidationContext(CaseTypeEntity parentCaseType) {
        this.caseReference = parentCaseType.getReference();
    }

    public String getCaseReference() {
        return caseReference;
    }
}
