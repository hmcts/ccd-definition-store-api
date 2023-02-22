package uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.Optional;

@Component
public class PredefinedComplexReferenceFieldTypeValidator implements FieldTypeValidator {

    @Override
    public ValidationResult validate(FieldTypeValidationContext context, FieldTypeEntity fieldType) {

        ValidationResult validationResult = new ValidationResult();

        if (null != fieldType.getJurisdiction()) {
            final Optional<FieldTypeEntity> referenceComplexConflict = context.getBaseComplexTypes()
                .stream()
                .filter(type -> type.getReference().equalsIgnoreCase(fieldType.getReference()))
                .findAny();
            if (referenceComplexConflict.isPresent()) {
                validationResult.addError(
                    new CannotOverridePredefinedComplexTypeValidationError(fieldType, referenceComplexConflict.get())
                );
            }
        }

        return validationResult;
    }

}
