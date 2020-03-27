package uk.gov.hmcts.ccd.definition.store.domain.validation.category;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CategoryEntity;

import java.util.List;

import static java.lang.String.format;

@Component
public class CategoryEntityDuplicateCategoryIDPerCaseTypeValidatorImpl implements CategoryEntityValidator {

    @Override
    public ValidationResult validate(final CategoryEntity categoryEntity,
                                     final CategoryEntityValidationContext categoryEntityValidationContext) {
        final ValidationResult validationResult = new ValidationResult();

        List<CategoryEntity> caseTypeCategories = categoryEntityValidationContext.getCategoryEntities();
        boolean hasDuplicate = caseTypeCategories.stream()
            .filter(e -> e != categoryEntity)
            .anyMatch(e -> e.getCategoryId().equals(categoryEntity.getCategoryId()));

        if (hasDuplicate) {
            validationResult.addError(new CategoryEntityDuplicateCategoryIDPerCaseTypeValidatorImpl
                .ValidationError(format("Duplicate CategoryID '%s' entry found for CaseType '%s'",
                categoryEntity.getCategoryId(), categoryEntityValidationContext.getCaseReference()), categoryEntity));
        }

        return validationResult;
    }

    public static class ValidationError extends SimpleValidationError<CategoryEntity> {

        public ValidationError(String defaultMessage, CategoryEntity entity) {
            super(defaultMessage, entity);
        }

        @Override
        public String createMessage(ValidationErrorMessageCreator validationErrorMessageCreator) {
            return validationErrorMessageCreator.createErrorMessage(this);
        }
    }

}
