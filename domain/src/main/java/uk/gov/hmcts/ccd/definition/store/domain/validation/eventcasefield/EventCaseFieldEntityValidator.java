package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;

public interface EventCaseFieldEntityValidator {

    ValidationResult validate(EventCaseFieldEntity eventCaseFieldEntity,
                              EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext);

    default boolean isReadOnlyDisplayContext(EventCaseFieldEntity eventCaseFieldEntity) {
        return eventCaseFieldEntity.getDisplayContext().equals(DisplayContext.READONLY);
    }

    default boolean isEmptyDisplayContext(EventCaseFieldEntity eventCaseFieldEntity) {
        return null == eventCaseFieldEntity.getDisplayContext();
    }
}
