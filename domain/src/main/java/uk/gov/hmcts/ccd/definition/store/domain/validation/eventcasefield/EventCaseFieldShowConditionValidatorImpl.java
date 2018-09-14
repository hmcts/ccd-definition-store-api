package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventCaseFieldShowConditionValidatorImpl implements EventCaseFieldEntityValidator {

    private final ShowConditionParser showConditionExtractor;

    @Autowired
    public EventCaseFieldShowConditionValidatorImpl(ShowConditionParser showConditionExtractor) {
        this.showConditionExtractor = showConditionExtractor;
    }

    @Override
    public ValidationResult validate(EventCaseFieldEntity eventCaseFieldEntity,
                                     EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext) {
        ValidationResult validationResult = new ValidationResult();

        if (StringUtils.isBlank(eventCaseFieldEntity.getShowCondition())) {
            return validationResult;
        }

        ShowCondition showCondition;
        try {
            showCondition = showConditionExtractor.parseShowCondition(eventCaseFieldEntity.getShowCondition());
        } catch (InvalidShowConditionException e) {
            validationResult.addError(
                new EventCaseFieldEntityInvalidShowConditionError(
                    eventCaseFieldEntity,
                    eventCaseFieldEntityValidationContext
                )
            );
            return validationResult;
        }

        showCondition.getFields().forEach(showConditionField -> {
            if (!showConditionFieldExistsAtLeastOneEventCaseFieldEntity(
                showConditionField,
                allOtherEventCaseFieldEntities(eventCaseFieldEntity, eventCaseFieldEntityValidationContext)
            )) {
                validationResult.addError(
                    new EventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError(
                        showConditionField,
                        eventCaseFieldEntityValidationContext,
                        eventCaseFieldEntity
                    )
                );
            }
        });

        return validationResult;
    }

    private List<EventCaseFieldEntity> allOtherEventCaseFieldEntities(EventCaseFieldEntity eventCaseFieldEntity,
                                                                      EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext) {
        return eventCaseFieldEntityValidationContext.getAllEventCaseFieldEntitiesForEventCase()
            .stream()
            .filter(
                ecfe ->
                    ecfe != eventCaseFieldEntity
            )
            .collect(Collectors.toList());
    }

    private boolean showConditionFieldExistsAtLeastOneEventCaseFieldEntity(String showConditionField,
                                                                           List<EventCaseFieldEntity> eventCaseFieldEntities) {
        return eventCaseFieldEntities
            .stream()
            .anyMatch(f -> f.getCaseField().getReference().equals(showConditionField));
    }

}
