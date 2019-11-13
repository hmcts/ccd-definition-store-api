package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldEntity;

import static java.util.Comparator.comparingInt;

@Component
public class ComplexFieldEntityOrderValidatorImpl implements CaseFieldEntityValidator {

    @Override
    public ValidationResult validate(CaseFieldEntity caseFieldEntity,
                                     CaseFieldEntityValidationContext caseFieldEntityValidationContext) {

        ValidationResult validationResult = new ValidationResult();

        validateComplexField(caseFieldEntity, validationResult, caseFieldEntity.getFieldType().getChildren());

        List<ComplexFieldEntity> children = caseFieldEntity.getFieldType().getChildren();
        children.forEach(child -> {
            validate(child);
        });

        if (!validationResult.isValid()) {
            return validationResult;
        }

        return validationResult;

    }

    private ValidationResult validate(ComplexFieldEntity complexField) {
        ValidationResult validationResult = new ValidationResult();

        if (complexField.isCompound()) {
            List<ComplexFieldEntity> children = complexField.getFieldType().getChildren();
            children.forEach(childField -> {
                if (childField.isCollectionFieldType()) {
                    validate(childField);
                } else if (childField.isComplexFieldType()) {
                    validate(childField);
                }
            });
            validateComplexField(complexField, validationResult, children);
        }
        return validationResult;
    }

    private void validateComplexField(FieldEntity fieldEntity, ValidationResult validationResult, List<ComplexFieldEntity> children) {
        List<ComplexFieldEntity> sortedFields = getSortedComplexFieldEntities(children);
        if (isChildOfComplexFieldMissingOrder(children, sortedFields)) {
            validationResult.addError(
                new ComplexFieldEntityMissingOrderValidationError(fieldEntity)
            );
        } else if (!sortedFields.isEmpty()) {
            validateOrdering(fieldEntity, validationResult, sortedFields);
        }
    }

    private void validateOrdering(FieldEntity fieldEntity, ValidationResult validationResult, List<ComplexFieldEntity> sortedFields) {
        IntStream.range(0, sortedFields.size()).forEach(index -> {
            if (sortedFields.get(index).getOrder() != index + 1) {
                validationResult.addError(
                    new ComplexFieldEntityIncorrectOrderValidationError(fieldEntity, index + 1, sortedFields.get(index))
                );
            }
        });
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
