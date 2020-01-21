package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Comparator.comparingInt;
import static java.util.stream.IntStream.range;

@Component
public class ComplexFieldEntityOrderValidatorImpl implements CaseFieldComplexFieldEntityValidator {

    @Override
    public ValidationResult validate(ComplexFieldEntity complexFieldEntity,
                                     ValidationContext validationContext) {

        ValidationResult validationResult = new ValidationResult();

        if (isOrderNotWithinSize(complexFieldEntity)) {
            validationResult.addError(new ComplexFieldEntityIncorrectOrderValidationError(complexFieldEntity));
        }

        validationResult.addErrors(validate(complexFieldEntity));

        return validationResult;

    }

    private boolean isOrderNotWithinSize(ComplexFieldEntity complexFieldEntity) {
        int sizeOfComplexType = complexFieldEntity.getComplexFieldType().getComplexFields().size();
        return complexFieldEntity.getOrder() != null && complexFieldEntity.getOrder() > sizeOfComplexType;
    }

    private List<ValidationError> validate(ComplexFieldEntity complexField) {
        List<ValidationError> validationErrors = newArrayList();

        if (complexField.isCompound()) {
            List<ComplexFieldEntity> children = complexField.getFieldType().getChildren();
            children.forEach(childField -> {
                if (childField.isCollectionFieldType()) {
                    validationErrors.addAll(validate(childField));
                } else if (childField.isComplexFieldType()) {
                    validationErrors.addAll(validate(childField));
                }
            });
            validateComplexField(complexField, children).ifPresent(validationErrors::add);
        }
        return validationErrors;
    }

    private Optional<ValidationError> validateComplexField(ComplexFieldEntity complexFieldEntity, List<ComplexFieldEntity> children) {
        Optional<ValidationError> validationErrorOptional = Optional.empty();
        List<ComplexFieldEntity> sortedFields = getSortedComplexFieldEntities(children);
        if (isChildOfComplexFieldMissingOrder(children, sortedFields)) {
            validationErrorOptional = Optional.of(new ComplexFieldEntityMissingOrderValidationError(complexFieldEntity));
        } else if (!sortedFields.isEmpty()) {
            if (isNotIncrementallyOrdered(sortedFields)) {
                validationErrorOptional = Optional.of(new ComplexFieldEntityIncorrectOrderValidationError(complexFieldEntity));
            }
        }
        return validationErrorOptional;
    }

    private boolean isNotIncrementallyOrdered(List<ComplexFieldEntity> sortedFields) {
        return range(0, sortedFields.size()).anyMatch(index -> sortedFields.get(index).getOrder() != index + 1);
    }

    private boolean isChildOfComplexFieldMissingOrder(List<ComplexFieldEntity> children, List<ComplexFieldEntity> sortedFields) {
        return sortedFields.size() != 0 && sortedFields.size() != children.size();
    }

    private List<ComplexFieldEntity> getSortedComplexFieldEntities(List<ComplexFieldEntity> children) {
        return children.stream()
                       .filter(field -> field.getOrder() != null)
                       .sorted(comparingInt(ComplexFieldEntity::getOrder))
                       .collect(Collectors.toList());
    }

}
