package uk.gov.hmcts.ccd.definition.store.domain.validation.authorization;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationContext;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

public class AuthorisationCaseFieldValidationContext implements ValidationContext {

    private final String caseReference;
    private final String caseFieldReference;

    public AuthorisationCaseFieldValidationContext(final CaseFieldEntity parentCaseField,
                                                   final CaseFieldEntityValidationContext parentContext) {
        this.caseReference = parentContext.getCaseReference();
        this.caseFieldReference = parentCaseField.getReference();
    }

    public String getCaseReference() {
        return caseReference;
    }

    public String getCaseFieldReference() {
        return caseFieldReference;
    }
}
