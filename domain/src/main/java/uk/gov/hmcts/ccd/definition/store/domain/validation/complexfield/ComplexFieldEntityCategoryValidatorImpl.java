package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
public class ComplexFieldEntityCategoryValidatorImpl implements ComplexFieldValidator {

    @Override
    public ValidationResult validate(ComplexFieldEntity complexFieldEntity,
                                     ValidationContext context) {

        ValidationResult validationResult = new ValidationResult();

        if (!complexFieldEntity.getFieldType().isDocumentType() && isNotBlank(complexFieldEntity.getCategoryId())) {
            validationResult.addError(
                new ComplexFieldEntityCategoryValidatorImpl.ValidationError(
                    format("Invalid complex field type '%s' for a categoryID '%s'",
                        complexFieldEntity.getFieldType().getReference(), complexFieldEntity.getCategoryId()),
                    complexFieldEntity)
            );
        }

        return validationResult;
    }

    public static class ValidationError extends SimpleValidationError<ComplexFieldEntity> {

        public ValidationError(String defaultMessage, ComplexFieldEntity entity) {
            super(defaultMessage, entity);
        }

        @Override
        public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
            return validationErrorMessageCreator.createErrorMessage(this);
        }
    }
}
