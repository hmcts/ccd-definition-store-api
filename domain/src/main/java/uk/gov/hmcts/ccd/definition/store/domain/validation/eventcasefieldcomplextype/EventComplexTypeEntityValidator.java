package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefieldcomplextype;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventComplexTypeEntity;

public interface EventComplexTypeEntityValidator {

    ValidationResult validate(EventComplexTypeEntity eventCaseFieldEntity,
                              EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext);

    default boolean isMandatoryDisplayContext(EventCaseFieldEntity eventCaseFieldEntity) {
        return eventCaseFieldEntity.getDisplayContext().equals(DisplayContext.MANDATORY);
    }

    default boolean isReadOnlyDisplayContext(EventCaseFieldEntity eventCaseFieldEntity) {
        return eventCaseFieldEntity.getDisplayContext().equals(DisplayContext.READONLY);
    }

    default boolean isEmptyDisplayContext(EventCaseFieldEntity eventCaseFieldEntity) {
        return null == eventCaseFieldEntity.getDisplayContext();
    }
}
