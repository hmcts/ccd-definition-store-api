package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.service.metadata.MetadataField;
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
    private final CaseFieldEntityUtil caseFieldEntityUtil;

    @Autowired
    public EventCaseFieldShowConditionValidatorImpl(ShowConditionParser showConditionExtractor,
                                                    CaseFieldEntityUtil caseFieldEntityUtil) {
        this.showConditionExtractor = showConditionExtractor;
        this.caseFieldEntityUtil = caseFieldEntityUtil;
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
                    eventCaseFieldEntityValidationContext));
            return validationResult;
        }

        List<EventCaseFieldEntity> allEventCaseFieldEntitiesForEventCase =
            eventCaseFieldEntityValidationContext.getAllEventCaseFieldEntitiesForEventCase();


        List<String> allSubTypePossibilities = caseFieldEntityUtil.buildDottedComplexFieldPossibilities(
            allEventCaseFieldEntitiesForEventCase.stream()
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
            if (!forShowConditionFieldExistsAtLeastOneEventCaseFieldEntity(
                showConditionField,
                allOtherEventCaseFieldEntities(eventCaseFieldEntity, allEventCaseFieldEntitiesForEventCase))
                && !MetadataField.isMetadataField(showConditionField)) {
                validationResult.addError(
                    new EventCaseFieldEntityWithShowConditionReferencesInvalidCaseFieldError(
                        showConditionField,
                        eventCaseFieldEntityValidationContext,
                        eventCaseFieldEntity));
            }
        });

        return validationResult;
    }

    private List<EventCaseFieldEntity> allOtherEventCaseFieldEntities(
        EventCaseFieldEntity eventCaseFieldEntity, List<EventCaseFieldEntity> allEventCaseFieldEntitiesForEventCase) {
        return allEventCaseFieldEntitiesForEventCase.stream()
            .filter(element -> element != eventCaseFieldEntity)
            .collect(Collectors.toList());
    }

    private boolean forShowConditionFieldExistsAtLeastOneEventCaseFieldEntity(
        String showConditionField, List<EventCaseFieldEntity> eventCaseFieldEntities) {
        return eventCaseFieldEntities
            .stream()
            .anyMatch(f -> f.getCaseField().getReference().equals(showConditionField));
    }
}
