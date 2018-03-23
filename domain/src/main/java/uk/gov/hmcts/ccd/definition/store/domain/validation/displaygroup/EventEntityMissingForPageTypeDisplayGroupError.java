package uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;

public class EventEntityMissingForPageTypeDisplayGroupError extends ValidationError {
    private final DisplayGroupEntity entity;

    public EventEntityMissingForPageTypeDisplayGroupError(DisplayGroupEntity displayGroupEntity) {
        super(String.format("Event cannot be null for displayGroup for type '%s'. Reference '%s' and with label '%s'",
            displayGroupEntity.getType(),
            displayGroupEntity.getReference(),
            displayGroupEntity.getLabel()));
        this.entity = displayGroupEntity;
    }

    public DisplayGroupEntity getCaseTypeUserRoleEntity() {
        return entity;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }
}
