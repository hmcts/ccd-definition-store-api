package uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class GenericLayoutEntityUniquenessValidatorImpl implements GenericLayoutValidator {

    static final String ERROR_MESSAGE_DUPLICATES_FOUND_FOR_TYPE_REF_FIELD_REF_AND_PATH =
        "Following have to be unique: [CaseTypeID '%s', CaseFieldID '%s', ListElementCode '%s'] with label '%s'";

    @Override
    public ValidationResult validate(List<GenericLayoutEntity> genericLayoutEntities) {
        final ValidationResult validationResult = new ValidationResult();

        for (GenericLayoutEntity entity : genericLayoutEntities) {
            if (entity.getCaseType() != null && entity.getCaseField() != null) {
                validateDuplicatesForTypeRefCaseRefAndPath(genericLayoutEntities, validationResult, entity);
            }
        }

        return validationResult;
    }

    private void validateDuplicatesForTypeRefCaseRefAndPath(final List<GenericLayoutEntity> genericLayouts,
                                                            final ValidationResult result,
                                                            final GenericLayoutEntity genericLayoutEntity) {
        genericLayouts.stream()
            .filter(e -> e != genericLayoutEntity)
            .filter(e -> e.getCaseField().getReference().equals(genericLayoutEntity.getCaseField().getReference()))
            .filter(e -> e.getCaseType().getReference().equals(genericLayoutEntity.getCaseType().getReference()))
            .filter(e -> StringUtils.equals(e.getCaseFieldElementPath(), genericLayoutEntity.getCaseFieldElementPath()))
            .findFirst().ifPresent(e -> result.addError(duplicateFoundError(e)));
    }

    private ValidationError duplicateFoundError(GenericLayoutEntity entity) {
        return new ValidationError(
            String.format(ERROR_MESSAGE_DUPLICATES_FOUND_FOR_TYPE_REF_FIELD_REF_AND_PATH,
                entity.getCaseType().getReference(),
                entity.getCaseField().getReference(),
                entity.getCaseFieldElementPath(),
                entity.getLabel()
            ), entity);
    }
}
