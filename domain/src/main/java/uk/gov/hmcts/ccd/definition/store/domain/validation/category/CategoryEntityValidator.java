package uk.gov.hmcts.ccd.definition.store.domain.validation.category;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CategoryEntity;

public interface CategoryEntityValidator {

    ValidationResult validate(CategoryEntity categoryEntity,
                              CategoryEntityValidationContext categoryEntityValidationContext);

}
