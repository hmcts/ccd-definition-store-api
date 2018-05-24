package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;

import java.util.Optional;

public interface FieldTypeToDisplayContextValidator {
    Optional<ValidationError> validate(EventCaseFieldEntity eventCaseFieldEntity);
}
