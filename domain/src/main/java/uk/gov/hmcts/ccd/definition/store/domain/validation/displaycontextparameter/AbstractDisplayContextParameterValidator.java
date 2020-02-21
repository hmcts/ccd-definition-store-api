package uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter;

import com.google.common.base.Strings;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;

import java.io.Serializable;
import java.util.Arrays;

public abstract class AbstractDisplayContextParameterValidator<T extends Serializable> {

    protected DisplayContextParameterValidatorFactory displayContextParameterValidatorFactory;

    protected static final String ERROR_MESSAGE_UNSUPPORTED_PARAMETER_TYPE =
        "Unsupported display context parameter type '%s' for field '%s'";
    protected static final String ERROR_MESSAGE_INVALID_VALUE =
        "Display context parameter '%s' has been incorrectly configured or is invalid for field '%s'";
    protected static final String ERROR_MESSAGE_UNSUPPORTED_FIELD_TYPE =
        "Display context parameter '%s' is unsupported for field type '%s' of field '%s'";

    private final DisplayContextParameterType[] ALLOWED_TYPES;
    private final String[] ALLOWED_FIELD_TYPES;

    protected abstract String getDisplayContextParameter(final T entity);
    protected abstract String getFieldType(final T entity);
    protected abstract ValidationError unsupportedDisplayContextParameterTypeError(final T entity);
    protected abstract ValidationError invalidValueError(final T entity);
    protected abstract ValidationError unsupportedFieldTypeError(final T entity);

    public AbstractDisplayContextParameterValidator(DisplayContextParameterValidatorFactory displayContextParameterValidatorFactory,
                                                    DisplayContextParameterType[] allowedTypes,
                                                    String[] allowedFieldTypes) {
        this.displayContextParameterValidatorFactory = displayContextParameterValidatorFactory;
        this.ALLOWED_TYPES = allowedTypes;
        this.ALLOWED_FIELD_TYPES = allowedFieldTypes;
    }

    public ValidationResult validate(T entity) {
        final ValidationResult validationResult = new ValidationResult();
        final String displayContextParameter = getDisplayContextParameter(entity);

        if (Strings.isNullOrEmpty(displayContextParameter)) {
            return validationResult;
        }

        validateCaseFieldType(entity, validationResult);
        if (!validationResult.isValid()) {
            return validationResult;
        }

        validateDisplayContextParameterType(entity, validationResult);
        if (!validationResult.isValid()) {
            return validationResult;
        }

        validateDisplayContextParameterValue(entity, validationResult);
        return validationResult;
    }

    private void validateCaseFieldType(final T entity, final ValidationResult validationResult) {
        String fieldType = getFieldType(entity);
        if (!Arrays.stream(ALLOWED_FIELD_TYPES).anyMatch(fieldType::equals)) {
            validationResult.addError(unsupportedFieldTypeError(entity));
        }
    }

    private void validateDisplayContextParameterType(final T entity, final ValidationResult validationResult) {
        DisplayContextParameterType parameterType = null;
        try {
            parameterType = DisplayContextParameterType.getParameterTypeFor(getDisplayContextParameter(entity));
        } catch (IllegalArgumentException e) {
            validationResult.addError(unsupportedDisplayContextParameterTypeError(entity));
            return;
        }
        if (!Arrays.stream(ALLOWED_TYPES).anyMatch(parameterType::equals)) {
            validationResult.addError(unsupportedDisplayContextParameterTypeError(entity));
        }
    }

    private void validateDisplayContextParameterValue(final T entity, final ValidationResult validationResult) {
        String parameterValue = DisplayContextParameterType.getParameterValueFor(getDisplayContextParameter(entity));
        try {
            DisplayContextParameterValidator parameterValidator = displayContextParameterValidatorFactory
                .getValidator(DisplayContextParameterType.getParameterTypeFor(getDisplayContextParameter(entity)));
            parameterValidator.validate(parameterValue);
        } catch (Exception e) {
            validationResult.addError(invalidValueError(entity));
        }
    }
}
