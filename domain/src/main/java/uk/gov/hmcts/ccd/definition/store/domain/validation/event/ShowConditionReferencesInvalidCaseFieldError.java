package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

public abstract class ShowConditionReferencesInvalidCaseFieldError extends ValidationError {

    private EventEntity eventEntity;

    private String showConditionField;

    public ShowConditionReferencesInvalidCaseFieldError(String message,
                                                        String showConditionField,
                                                        EventEntity eventEntity,
                                                        String showCondition) {
        super(
            String.format(
                message,
                showConditionField,
                eventEntity.getReference(),
                showCondition
            )
        );
        this.showConditionField = showConditionField;
        this.eventEntity = eventEntity;
    }

    public String getShowConditionField() {
        return showConditionField;
    }

    public EventEntity getEventEntity() {
        return eventEntity;
    }
}
