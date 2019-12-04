package uk.gov.hmcts.ccd.definition.store.domain.validation.authorization;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationContext;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

public class AuthorisationCaseFieldValidationContext implements ValidationContext {

    private final String caseRole;
    private final String caseReference;
    private final String caseFieldReference;

    public AuthorisationCaseFieldValidationContext(final CaseFieldEntity parentCaseField,
                                                   final CaseFieldEntityValidationContext parentContext) {
        this.caseRole = null;
        this.caseReference = parentContext.getCaseReference();
        this.caseFieldReference = parentCaseField.getReference();
    }

    public AuthorisationCaseFieldValidationContext(final String caseRole,
                                                   final CaseFieldEntity parentCaseField,
                                                   final CaseFieldEntityValidationContext parentContext) {
        this.caseRole = caseRole;
        this.caseReference = parentContext.getCaseReference();
        this.caseFieldReference = parentCaseField.getReference();
    }

    public String getCaseRole() {
        return caseRole;
    }

    public String getCaseReference() {
        return caseReference;
    }

    public String getCaseFieldReference() {
        return caseFieldReference;
    }
}
