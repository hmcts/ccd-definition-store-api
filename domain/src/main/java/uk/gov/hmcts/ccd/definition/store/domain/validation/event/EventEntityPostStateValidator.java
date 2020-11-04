package uk.gov.hmcts.ccd.definition.store.domain.validation.event;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.service.metadata.MetadataField;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventPostStateEntity;

@Component
public class EventEntityPostStateValidator implements EventEntityValidator {

    private final ShowConditionParser showConditionExtractor;
    private final CaseFieldEntityUtil caseFieldEntityUtil;

    @Autowired
    public EventEntityPostStateValidator(final ShowConditionParser showConditionExtractor,
                                         CaseFieldEntityUtil caseFieldEntityUtil) {
        this.showConditionExtractor = showConditionExtractor;
        this.caseFieldEntityUtil = caseFieldEntityUtil;
    }

    @Override
    public ValidationResult validate(final EventEntity eventEntity,
                                     final EventEntityValidationContext eventEntityValidationContext) {

        final ValidationResult validationResult = new ValidationResult();

        List<EventPostStateEntity> postStateEntities = eventEntity.getPostStates();

        if (postStateEntities.isEmpty()) {
            return validationResult;
        }

        validateNonConditionalPostState(eventEntity,
            validationResult,
            postStateEntities,
            eventEntityValidationContext);

        validatePostStateDuplicatePriorities(eventEntity,
            validationResult,
            postStateEntities,
            eventEntityValidationContext);

        validateShowConditionFields(eventEntity, validationResult);
        return validationResult;
    }

    private void validateShowConditionFields(EventEntity eventEntity,
                                             ValidationResult validationResult) {
        List<EventPostStateEntity> postStateEntities = eventEntity.getPostStates();

        postStateEntities
            .stream()
            .filter(postStateEntity -> postStateEntity.getEnablingCondition() != null)
            .forEach(entity -> {
                try {
                    ShowCondition showCondition = showConditionExtractor
                        .parseShowCondition(entity.getEnablingCondition());

                    List<String> allSubTypePossibilities = caseFieldEntityUtil
                        .buildDottedComplexFieldPossibilities(eventEntity.getCaseType().getCaseFields());

                    showCondition.getFieldsWithSubtypes().forEach(showConditionField -> {
                        if (!allSubTypePossibilities.contains(showConditionField)) {
                            validationResult.addError(new EventEntityShowConditionReferencesInvalidCaseFieldError(
                                showConditionField,
                                eventEntity,
                                entity.getEnablingCondition()
                            ));
                        }
                    });

                    showCondition.getFields().forEach(showConditionField -> {
                        if (!forShowConditionFieldExistsAtLeastOneCaseFieldEntity(
                            showConditionField,
                            eventEntity.getCaseType().getCaseFields())
                            && !MetadataField.isMetadataField(showConditionField)) {
                            validationResult.addError(new EventEntityShowConditionReferencesInvalidCaseFieldError(
                                showConditionField,
                                eventEntity,
                                entity.getEnablingCondition()
                            ));
                        }
                    });
                } catch (InvalidShowConditionException e) {
                    // this is handled during parsing. here no exceptions will be thrown
                }
            });
    }

    private void validateNonConditionalPostState(EventEntity event,
                                                 ValidationResult validationResult,
                                                 List<EventPostStateEntity> postStateEntities,
                                                 EventEntityValidationContext eventEntityValidationContext) {
        Optional<EventPostStateEntity> conditionalEntity = postStateEntities
            .stream()
            .filter(postStateEntity -> postStateEntity.getEnablingCondition() != null)
            .findAny();

        Optional<EventPostStateEntity> defaultEntity = postStateEntities
            .stream()
            .filter(postStateEntity -> postStateEntity.getEnablingCondition() == null)
            .findAny();
        if (conditionalEntity.isPresent() && defaultEntity.isEmpty()) {
            validationResult.addError(new EventEntityInvalidDefaultPostStateError(event, eventEntityValidationContext));
        }
    }

    private void validatePostStateDuplicatePriorities(EventEntity event,
                                                      ValidationResult validationResult,
                                                      List<EventPostStateEntity> postStateEntities,
                                                      EventEntityValidationContext eventEntityValidationContext) {
        List<EventPostStateEntity> postStateEntitiesUniquePriority = postStateEntities
            .stream()
            .filter(distinctByKey(postState -> postState.getPriority()))
            .collect(Collectors.toList());

        if (postStateEntitiesUniquePriority.size() != postStateEntities.size()) {
            validationResult.addError(new EventEntityInvalidPostStatePriorityError(
                event,
                eventEntityValidationContext));
        }
    }

    private boolean forShowConditionFieldExistsAtLeastOneCaseFieldEntity(String showConditionField,
                                                                         List<CaseFieldEntity> caseFieldEntities) {
        return caseFieldEntities
            .stream()
            .anyMatch(f -> f.getReference().equals(showConditionField));
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> duplicates = new ConcurrentHashMap<>();
        return t -> duplicates.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
