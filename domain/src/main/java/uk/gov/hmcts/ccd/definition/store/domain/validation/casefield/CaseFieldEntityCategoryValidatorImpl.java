package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

import java.util.regex.Pattern;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
public class CaseFieldEntityCategoryValidatorImpl implements CaseFieldEntityValidator {

    // TODO: RDM-7984 FixedLists will extend the pattern to "^#?[a-zA-Z_0-9]+"
    private static final Pattern CATEGORY_ID_REGULAR_EXPRESSION_PATTERN = Pattern.compile("^[a-zA-Z_0-9]+");

    @Override
    public ValidationResult validate(CaseFieldEntity caseField,
                                     CaseFieldEntityValidationContext context) {

        ValidationResult validationResult = new ValidationResult();

        if (isNotBlank(caseField.getCategoryId())) {
            boolean isDocumentType = validateDocumentType(caseField, validationResult);
            if (isDocumentType) {
                boolean validFormat = validateCategoryIdFormat(caseField, validationResult);
                if (validFormat) {
                    validateIfCategoryIdDefined(caseField, context, validationResult);
                }
            }
        }

        return validationResult;
    }

    private boolean validateDocumentType(CaseFieldEntity caseField, ValidationResult validationResult) {
        if (!caseField.getFieldType().isDocumentType()) {
            validationResult.addError(
                new ValidationError(
                    format("Invalid case field type '%s' for a categoryID '%s' for caseType '%s'",
                        caseField.getFieldType().getReference(), caseField.getCategoryId(),
                        caseField.getCaseType().getReference()), caseField)
            );
            return false;
        }
        return true;
    }

    private boolean validateCategoryIdFormat(CaseFieldEntity caseField, ValidationResult validationResult) {
        if (!CATEGORY_ID_REGULAR_EXPRESSION_PATTERN.matcher(caseField.getCategoryId()).matches()) {
            validationResult.addError(
                new ValidationError(
                    format("Invalid format of CategoryID '%s' on case field '%s' for caseType '%s'",
                        caseField.getCategoryId(), caseField.getReference(), caseField.getCaseType().getReference()),
                    caseField)
            );
            return false;
        }
        return true;
    }

    private void validateIfCategoryIdDefined(CaseFieldEntity caseField, CaseFieldEntityValidationContext context, ValidationResult validationResult) {
        if (!context.getCategories().contains(caseField.getCategoryId())) {
            validationResult.addError(
                new ValidationError(
                    format("CategoryID '%s' not defined in Category tab. Case field '%s', caseType '%s'",
                        caseField.getCategoryId(), caseField.getReference(), caseField.getCaseType().getReference()),
                    caseField)
            );
        }
    }

    public static class ValidationError extends SimpleValidationError<CaseFieldEntity> {

        public ValidationError(String defaultMessage, CaseFieldEntity entity) {
            super(defaultMessage, entity);
        }

        @Override
        public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
            return validationErrorMessageCreator.createErrorMessage(this);
        }
    }
}
