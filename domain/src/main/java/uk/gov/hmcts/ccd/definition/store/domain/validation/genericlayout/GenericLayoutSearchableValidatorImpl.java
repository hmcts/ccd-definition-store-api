package uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.LayoutSheetType;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;

import java.util.List;

@Component
public class GenericLayoutSearchableValidatorImpl implements GenericLayoutValidator {

    @Override
    public ValidationResult validate(GenericLayoutEntity genericLayoutEntity,
                                     List<GenericLayoutEntity> allGenericLayouts) {
        ValidationResult validationResult = new ValidationResult();

        if (genericLayoutEntity.getLayoutSheetType() != LayoutSheetType.INPUT) {
            return validationResult;
        }

        if (!genericLayoutEntity.isSearchable()) {
            validationResult.addError(new GenericLayoutValidator.ValidationError(
                String.format("Layout sheet '%s' contains a non-searchable field '%s'.",
                    genericLayoutEntity.getSheetName(), genericLayoutEntity.buildFieldPath()),
                genericLayoutEntity));
        }

        return validationResult;
    }
}
