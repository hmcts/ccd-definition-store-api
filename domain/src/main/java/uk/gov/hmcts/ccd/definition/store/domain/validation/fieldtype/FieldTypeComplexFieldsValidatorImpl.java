package uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield.ComplexFieldValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.List;

@Component
public class FieldTypeComplexFieldsValidatorImpl implements FieldTypeValidator {

    private List<ComplexFieldValidator> complexFieldValidators;

    @Autowired
    public FieldTypeComplexFieldsValidatorImpl(List<ComplexFieldValidator> complexFieldValidators) {
        this.complexFieldValidators = complexFieldValidators;
    }

    @Override
    public ValidationResult validate(FieldTypeValidationContext context, FieldTypeEntity fieldType) {
        ValidationResult validationResult = new ValidationResult();
        ComplexFieldValidator.ValidationContext validationContext =
            new ComplexFieldValidator.ValidationContext(fieldType);
        for (ComplexFieldEntity complexField : fieldType.getComplexFields()) {
            for (ComplexFieldValidator complexFieldValidator : complexFieldValidators) {
                validationResult.merge(complexFieldValidator.validate(complexField, validationContext));
            }
        }
        return validationResult;
    }
}
