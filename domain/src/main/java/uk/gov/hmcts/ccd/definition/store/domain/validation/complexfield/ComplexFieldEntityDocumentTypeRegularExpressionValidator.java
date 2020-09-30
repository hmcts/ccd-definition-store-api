package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.regex.AbstractDocumentTypeRegularExpressionValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;

@Component
public class ComplexFieldEntityDocumentTypeRegularExpressionValidator
    extends AbstractDocumentTypeRegularExpressionValidator implements ComplexFieldValidator {

    @Override
    public ValidationResult validate(ComplexFieldEntity complexField, ValidationContext validationContext) {
        ValidationResult validationResult = new ValidationResult();
        validateDocumentType(complexField.getFieldType(), validationResult);

        return validationResult;
    }
}
