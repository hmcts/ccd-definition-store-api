package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;

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

        if (isBlank(eventCaseFieldEntity.getShowCondition())) {
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

        List<String> allSubTypePossibilities = CaseFieldEntityUtil.buildDottedComplexFieldPossibilities(
            eventCaseFieldEntityValidationContext.getAllEventCaseFieldEntitiesForEventCase().stream()
                .map(EventCaseFieldEntity::getCaseField)
                .collect(Collectors.toList()));

        showCondition.getFieldsWithSubtypes().forEach(showConditionField -> {
            if (!allSubTypePossibilities.contains(showConditionField)) {
                validationResult.addError(
                    new EventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError(
                        showConditionField,
                        eventCaseFieldEntityValidationContext,
                        eventCaseFieldEntity
                    ));
            }
        });

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
            .filter(ecfe -> ecfe != eventCaseFieldEntity)
            .collect(Collectors.toList());
    }

    private boolean showConditionFieldExistsAtLeastOneEventCaseFieldEntity(String showConditionField,
                                                                           List<EventCaseFieldEntity> eventCaseFieldEntities) {
        return eventCaseFieldEntities
            .stream()
            .anyMatch(f -> f.getCaseField().getReference().equals(showConditionField));
    }

}
