package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

import static java.lang.String.format;

@Component
public class CaseFieldEntityCategoryValidatorImpl implements CaseFieldEntityValidator {

    @Override
    public ValidationResult validate(CaseFieldEntity caseField,
                                     CaseFieldEntityValidationContext context) {

        ValidationResult validationResult = new ValidationResult();

        if (!caseField.getFieldType().isDocumentType() && StringUtils.isNotBlank(caseField.getCategoryId())) {
            validationResult.addError(
                new CaseFieldEntityCategoryValidatorImpl.ValidationError(
                    format("Invalid case field type '%s' for a categoryID '%s' for caseType '%s'",
                        caseField.getFieldType().getReference(), caseField.getCategoryId(),
                        caseField.getCaseType().getReference()), caseField)
            );
        }

        return validationResult;
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
