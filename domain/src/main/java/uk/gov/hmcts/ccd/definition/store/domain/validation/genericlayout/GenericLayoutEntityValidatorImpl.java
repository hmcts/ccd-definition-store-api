package uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;

import java.util.List;

@Component
public class GenericLayoutEntityValidatorImpl implements GenericLayoutValidator {

    private static final String ERROR_MESSAGE_INVALID_CASE_TYPE_NOT_PRESENT =
        "Case Type cannot be empty for row with label '%s', case field '%s'";
    private static final String ERROR_MESSAGE_CASE_FIELD_NOT_PRESENT =
        "Case Field cannot be empty for row with label '%s', case type '%s'";

    @Override
    public ValidationResult validate(GenericLayoutEntity entity, List<GenericLayoutEntity> allGenericLayouts) {
        final ValidationResult validationResult = new ValidationResult();

        validateCaseTypeIsPresent(entity, validationResult);
        validateCaseFieldIsPresent(entity, validationResult);

        return validationResult;
    }

    private void validateCaseTypeIsPresent(final GenericLayoutEntity entity, final ValidationResult validationResult) {
        if (entity.getCaseType() == null) {
            validationResult.addError(caseTypeNotPresentError(entity));
        }
    }

    private void validateCaseFieldIsPresent(final GenericLayoutEntity entity, final ValidationResult validationResult) {
        if (entity.getCaseField() == null) {
            validationResult.addError(caseFieldNotPresentError(entity));
        }
    }

    private ValidationError caseFieldNotPresentError(final GenericLayoutEntity entity) {
        return new ValidationError(
            String.format(ERROR_MESSAGE_CASE_FIELD_NOT_PRESENT,
                entity.getLabel(),
                (entity.getCaseType() != null ? entity.getCaseType().getReference() : "")
            ), entity);
    }

    private ValidationError caseTypeNotPresentError(final GenericLayoutEntity entity) {
        return new ValidationError(
            String.format(ERROR_MESSAGE_INVALID_CASE_TYPE_NOT_PRESENT,
                entity.getLabel(),
                (entity.getCaseField() != null ? entity.getCaseField().getReference() : "")
            ), entity);
    }
}
