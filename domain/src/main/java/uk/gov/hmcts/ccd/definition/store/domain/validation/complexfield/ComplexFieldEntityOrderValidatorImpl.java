package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

import static java.util.Comparator.comparingInt;
import static java.util.stream.IntStream.range;

@Component
public class ComplexFieldEntityOrderValidatorImpl implements CaseFieldEntityValidator {

    @Override
    public ValidationResult validate(CaseFieldEntity caseFieldEntity,
                                     CaseFieldEntityValidationContext caseFieldEntityValidationContext) {

        ValidationResult validationResult = new ValidationResult();

        List<ComplexFieldEntity> children = caseFieldEntity.getFieldType().getChildren();

        validateComplexField(caseFieldEntity, children).ifPresent(validationResult::addError);

        children.forEach(child -> {
            validationResult.addErrors(validate(child));
        });

        return validationResult;

    }

    private List<ValidationError> validate(ComplexFieldEntity complexField) {
        List<ValidationError> validationErrors = Lists.newArrayList();

        if (complexField.isCompound()) {
            List<ComplexFieldEntity> children = complexField.getFieldType().getChildren();
            children.forEach(childField -> {
                if (childField.isCollectionFieldType()) {
                    validate(childField);
                } else if (childField.isComplexFieldType()) {
                    validate(childField);
                }
            });
            validateComplexField(complexField, children).ifPresent(validationErrors::add);
        }
        return validationErrors;
    }

    private Optional<ValidationError> validateComplexField(FieldEntity fieldEntity, List<ComplexFieldEntity> children) {
        Optional<ValidationError> validationErrorOptional = Optional.empty();
        List<ComplexFieldEntity> sortedFields = getSortedComplexFieldEntities(children);
        if (isChildOfComplexFieldMissingOrder(children, sortedFields)) {
            validationErrorOptional = Optional.of(new ComplexFieldEntityMissingOrderValidationError(fieldEntity.getFieldType()));
        } else if (!sortedFields.isEmpty()) {
            if (isNotIncrementallyOrdered(sortedFields)) {
                validationErrorOptional = Optional.of(new ComplexFieldEntityIncorrectOrderValidationError(fieldEntity.getFieldType()));
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
