package uk.gov.hmcts.ccd.definition.store.domain.validation.state;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;

public interface StateEntityValidator {

    ValidationResult validate(StateEntity stateEntity,
                              StateEntityValidationContext stateEntityValidationContext);

}
