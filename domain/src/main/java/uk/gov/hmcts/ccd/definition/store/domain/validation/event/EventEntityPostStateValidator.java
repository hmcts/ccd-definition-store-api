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
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventPostStateEntity;

@Component
public class EventEntityPostStateValidator extends AbstractShowConditionValidator {

    @Autowired
    public EventEntityPostStateValidator(final ShowConditionParser showConditionExtractor,
                                         CaseFieldEntityUtil caseFieldEntityUtil) {
        super(showConditionExtractor, caseFieldEntityUtil);
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
            .forEach(entity -> validateShowConditionFields(eventEntity,
                validationResult,
                entity.getEnablingCondition()));
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

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> duplicates = new ConcurrentHashMap<>();
        return t -> duplicates.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    public ValidationError getValidationError(String showConditionField,
                                              EventEntity eventEntity,
                                              String showCondition) {
        return new EventEntityShowConditionReferencesInvalidCaseFieldError(showConditionField,
            eventEntity,
            showCondition);
    }
}
