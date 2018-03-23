package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

public interface EventEntityValidator {

    ValidationResult validate(EventEntity caseEvent,
                              EventEntityValidationContext eventEntityValidationContext);

}
