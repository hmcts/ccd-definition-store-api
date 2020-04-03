package uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GenericLayoutEntityOrderValidatorImpl implements GenericLayoutValidator {

    private static final String ERROR_MESSAGE_INVALID_NUMBER_WITH_CASE_FIELD =
        "DisplayOrder '%d' needs to be a valid integer for row with label '%s', case field '%s'";

    @Override
    public ValidationResult validate(GenericLayoutEntity entity, List<GenericLayoutEntity> allGenericLayouts) {
        final ValidationResult validationResult = new ValidationResult();

        validateOrder(entity, validationResult);

        return validationResult;
    }

    private void validateOrder(final GenericLayoutEntity entity, final ValidationResult validationResult) {
        if (entity.getCaseField() != null && entity.getOrder() != null && entity.getOrder() < 1) {
            final String errorMessage =
                String.format(ERROR_MESSAGE_INVALID_NUMBER_WITH_CASE_FIELD,
                    entity.getOrder(),
                    entity.getLabel(),
                    entity.getCaseField().getReference());

            validationResult.addError(new ValidationError(errorMessage, entity));
        }
    }
}
