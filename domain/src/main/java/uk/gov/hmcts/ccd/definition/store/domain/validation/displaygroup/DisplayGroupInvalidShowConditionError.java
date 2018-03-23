package uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;

public class DisplayGroupInvalidShowConditionError extends ValidationError {

    private DisplayGroupEntity displayGroupEntity;

    public DisplayGroupInvalidShowConditionError(DisplayGroupEntity displayGroupEntity) {
        super(
            String.format(
                "Invalid show condition '%s' for display group '%s'",
                displayGroupEntity.getShowCondition(), displayGroupEntity.getReference()
            )
        );
        this.displayGroupEntity = displayGroupEntity;
    }

    @Override
    public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
        return validationErrorMessageCreator.createErrorMessage(this);
    }

    public DisplayGroupEntity getDisplayGroupEntity() {
        return this.displayGroupEntity;
    }

}
