package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ComplexFieldEntitySecurityClassificationValidatorImpl implements CaseFieldComplexFieldEntityValidator {

    public ValidationResult validate(ComplexFieldEntity complexField) {
        ValidationResult validationResult = new ValidationResult();

        if (complexField.getSecurityClassification() == null) {
            validationResult.addError(
                new ComplexFieldEntityMissingSecurityClassificationValidationError(complexField)
            );
        }

        return validationResult;
    }

    @Override
    public ValidationResult validate(ComplexFieldEntity complexField,
                                     ValidationContext validationContext) {

        ValidationResult validationResult = validate(complexField);

        if (!validationResult.isValid()) {
            return validationResult;
        }


        SecurityClassification parentSecurityClassification = validationContext.getParentSecurityClassification();

        if (isNotPredefinedComplexType(complexField, validationContext.getPreDefinedComplexTypes())
            && parentSecurityClassification != null
            && parentSecurityClassification.isMoreRestrictiveThan(complexField.getSecurityClassification())) {
            validationResult.addError(
                new ComplexFieldEntityHasLessRestrictiveSecurityClassificationThanParentValidationError(
                    complexField, validationContext)
            );
        }

        return validationResult;

    }

    private boolean isNotPredefinedComplexType(ComplexFieldEntity complexField,
                                               List<FieldTypeEntity> predefinedComplexTypes) {
        return complexField.getComplexFieldType() == null
            || !predefinedComplexTypes.stream().map(FieldTypeEntity::getReference)
            .collect(Collectors.toList()).contains(complexField.getComplexFieldType().getReference());
    }
}
