package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefieldcomplextype;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventComplexTypeEntity;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class EventComplexTypeShowConditionValidatorImpl implements EventComplexTypeEntityValidator {

    private final ShowConditionParser showConditionExtractor;

    @Autowired
    public EventComplexTypeShowConditionValidatorImpl(ShowConditionParser showConditionExtractor) {
        this.showConditionExtractor = showConditionExtractor;
    }

    @Override
    public ValidationResult validate(EventComplexTypeEntity eventCaseFieldEntity,
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
                new EventComplexTypeEntityInvalidShowConditionError(
                    eventCaseFieldEntity,
                    eventCaseFieldEntityValidationContext
                ));
            return validationResult;
        }

        List<EventCaseFieldEntity> allEventCaseFieldEntitiesForEventCase =
            eventCaseFieldEntityValidationContext.getAllEventCaseFieldEntitiesForEventCase();

        List<String> allSubTypePossibilities = CaseFieldEntityUtil.buildDottedComplexFieldPossibilities(
            allEventCaseFieldEntitiesForEventCase.stream()
                .map(EventCaseFieldEntity::getCaseField)
                .collect(Collectors.toList()));

        showCondition.getFieldsWithSubtypes().forEach(showConditionField -> {
            if (!allSubTypePossibilities.contains(showConditionField)) {
                validationResult.addError(
                    new EventComplexTypeEntityWithShowConditionReferencesInvalidCaseFieldError(
                        showConditionField,
                        eventCaseFieldEntityValidationContext,
                        eventCaseFieldEntity));
            }
        });

        return validationResult;
    }
}
