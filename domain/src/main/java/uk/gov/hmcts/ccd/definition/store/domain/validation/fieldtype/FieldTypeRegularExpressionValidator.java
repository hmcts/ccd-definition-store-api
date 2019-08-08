package uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.regex.AbstractRegularExpressionValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

@Component
public class FieldTypeRegularExpressionValidator extends AbstractRegularExpressionValidator implements FieldTypeValidator {

    @Override
    public ValidationResult validate(FieldTypeValidationContext context, FieldTypeEntity fieldType) {
        ValidationResult validationResult = new ValidationResult();

        validateDocumentType(fieldType, validationResult);

        return validationResult;
    }

}
