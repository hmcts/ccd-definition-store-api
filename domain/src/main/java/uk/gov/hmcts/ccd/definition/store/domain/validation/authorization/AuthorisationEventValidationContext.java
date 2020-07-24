package uk.gov.hmcts.ccd.definition.store.domain.validation.authorization;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationContext;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

public class AuthorisationEventValidationContext implements ValidationContext {

    private final String caseReference;
    private final String eventReference;

    public AuthorisationEventValidationContext(final EventEntity parentEvent,
                                               final EventEntityValidationContext parentContext) {
        this.caseReference = parentContext.getCaseReference();
        this.eventReference = parentEvent.getReference();
    }

    public String getCaseReference() {
        return caseReference;
    }

    public String getEventReference() {
        return eventReference;
    }
}
