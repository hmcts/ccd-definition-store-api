package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.category.CategoryEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.domain.validation.category.CategoryEntityValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CategoryEntity;

import java.util.List;

@Component
public class CaseTypeEntityCategoryValidatorImpl implements CaseTypeEntityValidator {

    private List<CategoryEntityValidator> categoryEntityValidators;

    @Autowired
    public CaseTypeEntityCategoryValidatorImpl(List<CategoryEntityValidator> categoryEntityValidators) {
        this.categoryEntityValidators = categoryEntityValidators;
    }

    @Override
    public ValidationResult validate(CaseTypeEntity caseType) {

        ValidationResult validationResult = new ValidationResult();

        for (CategoryEntityValidator categoryEntityValidator : categoryEntityValidators) {
            for (CategoryEntity categoryEntity : caseType.getCategories()) {
                validationResult.merge(categoryEntityValidator.validate(
                    categoryEntity,
                    new CategoryEntityValidationContext(caseType)
                    )
                );
            }
        }
        return validationResult;
    }
}
