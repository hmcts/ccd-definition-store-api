package uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;

import java.util.regex.Pattern;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
public class ComplexFieldEntityCategoryValidatorImpl implements ComplexFieldValidator {

    // TODO: RDM-7984 FixedLists will extend the pattern to "^#?[a-zA-Z_0-9]+"
    private static final Pattern CATEGORY_ID_REGULAR_EXPRESSION_PATTERN = Pattern.compile("^[a-zA-Z_0-9]+");

    @Override
    public ValidationResult validate(ComplexFieldEntity complexFieldEntity,
                                     ValidationContext context) {

        ValidationResult validationResult = new ValidationResult();

        if (isNotBlank(complexFieldEntity.getCategoryId())) {
            boolean isDocumentType = validateDocumentType(complexFieldEntity, validationResult);
            if (isDocumentType) {
                validateCategoryIdFormat(complexFieldEntity, validationResult);
            }

            // TODO: there is no access to CaseType, that holds Categories. Think how to fix this.
//            validateIfCategoryIdDefined(complexFieldEntity, context, validationResult);
        }

        return validationResult;
    }

    private boolean validateDocumentType(ComplexFieldEntity complexFieldEntity, ValidationResult validationResult) {
        if (!complexFieldEntity.getFieldType().isDocumentType()) {
            validationResult.addError(
                new ComplexFieldEntityCategoryValidatorImpl.ValidationError(
                    format("Invalid ComplexTypes FieldType '%s' for a categoryID '%s'.",
                        complexFieldEntity.getFieldType().getReference(), complexFieldEntity.getCategoryId()),
                    complexFieldEntity)
            );
            return false;
        }
        return true;
    }

    private boolean validateCategoryIdFormat(ComplexFieldEntity complexFieldEntity, ValidationResult validationResult) {
        if (!CATEGORY_ID_REGULAR_EXPRESSION_PATTERN.matcher(complexFieldEntity.getCategoryId()).matches()) {
            validationResult.addError(
                new ComplexFieldEntityCategoryValidatorImpl.ValidationError(
                    format("Invalid format of CategoryID '%s' on complex field '%s'",
                        complexFieldEntity.getCategoryId(), complexFieldEntity.getReference()),
                    complexFieldEntity)
            );
            return false;
        }
        return true;
    }

    private void validateIfCategoryIdDefined(ComplexFieldEntity complexFieldEntity, ValidationContext context, ValidationResult validationResult) {
//        if (!context.getCategories().contains(complexFieldEntity.getCategoryId())) {
//            validationResult.addError(
//                new ComplexFieldEntityCategoryValidatorImpl.ValidationError(
//                    format("CategoryID '%s' not defined in Category tab. Complex field '%s'",
//                        complexFieldEntity.getCategoryId(), complexFieldEntity.getReference()),
//                    complexFieldEntity)
//            );
//        }
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
