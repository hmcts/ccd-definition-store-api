package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.regex.AbstractRegularExpressionValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;

@Component
public class ComplexFieldEntityRegularExpressionValidator extends AbstractRegularExpressionValidator implements ComplexFieldValidator {

    @Override
    public ValidationResult validate(ComplexFieldEntity complexField, ValidationContext validationContext) {
        ValidationResult validationResult = new ValidationResult();
        validateDocumentType(complexField.getFieldType(), validationResult);

        return validationResult;
    }
}
