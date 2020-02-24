package uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter;

import com.google.common.base.Strings;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameter;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;
import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Optional;

public abstract class AbstractDisplayContextParameterValidator<T extends Serializable> {

    protected static final String ERROR_MESSAGE_UNSUPPORTED_PARAMETER_TYPE =
        "Unsupported display context parameter type '%s' for field '%s' on tab '%s'";
    protected static final String ERROR_MESSAGE_INVALID_VALUE =
        "Display context parameter '%s' has been incorrectly configured or is invalid for field '%s' on tab '%s'";
    protected static final String ERROR_MESSAGE_UNSUPPORTED_FIELD_TYPE =
        "Display context parameter '%s' is unsupported for field type '%s' of field '%s' on tab '%s'";

    private final DisplayContextParameterType[] ALLOWED_TYPES;
    private final String[] ALLOWED_FIELD_TYPES;

    protected DisplayContextParameterValidatorFactory displayContextParameterValidatorFactory;

    public AbstractDisplayContextParameterValidator(DisplayContextParameterValidatorFactory displayContextParameterValidatorFactory,
                                                    DisplayContextParameterType[] allowedTypes,
                                                    String[] allowedFieldTypes) {
        this.displayContextParameterValidatorFactory = displayContextParameterValidatorFactory;
        this.ALLOWED_TYPES = allowedTypes;
        this.ALLOWED_FIELD_TYPES = allowedFieldTypes;
    }

    protected abstract String getDisplayContextParameter(final T entity);

    protected abstract String getFieldType(final T entity);

    protected abstract String getCaseFieldReference(final T entity);

    protected abstract String getSheetName(final T entity);

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

        validateDisplayContextParameter(entity, validationResult);
        return validationResult;
    }

    private void validateCaseFieldType(final T entity, final ValidationResult validationResult) {
        String fieldType = getFieldType(entity);
        if (Arrays.stream(ALLOWED_FIELD_TYPES).noneMatch(fieldType::equals)) {
            validationResult.addError(unsupportedFieldTypeError(entity));
        }
    }

    private void validateDisplayContextParameter(final T entity, final ValidationResult validationResult) {
        Optional<DisplayContextParameter> displayContextParameter =
            DisplayContextParameterType.getDisplayContextParameterFor(getDisplayContextParameter(entity));
        if (displayContextParameter.isPresent()) {
            validateDisplayContextParameterType(displayContextParameter.get(), entity, validationResult);
            if (!validationResult.isValid()) {
                return;
            }
            validateDisplayContextParameterValue(displayContextParameter.get(), entity, validationResult);
        } else {
            validationResult.addError(
                new SimpleValidationError<>(String.format(
                    ERROR_MESSAGE_INVALID_VALUE,
                    getDisplayContextParameter(entity),
                    getCaseFieldReference(entity),
                    getSheetName(entity)
                ), entity)
            );
        }
    }

    private void validateDisplayContextParameterType(final DisplayContextParameter displayContextParameter,
                                                     final T entity,
                                                     final ValidationResult validationResult) {
        if (Arrays.stream(ALLOWED_TYPES).noneMatch(displayContextParameter.getType()::equals)) {
            validationResult.addError(unsupportedDisplayContextParameterTypeError(entity));
        }
    }

    private void validateDisplayContextParameterValue(final DisplayContextParameter displayContextParameter,
                                                      final T entity,
                                                      final ValidationResult validationResult) {
        DisplayContextParameterValidator parameterValidator = displayContextParameterValidatorFactory
            .getValidator(displayContextParameter.getType());
        try {
            parameterValidator.validate(displayContextParameter.getValue());
        } catch (Exception e) {
            validationResult.addError(invalidValueError(entity));
        }
    }

    private ValidationError unsupportedDisplayContextParameterTypeError(final T entity) {
        return new SimpleValidationError<>(
            String.format(ERROR_MESSAGE_UNSUPPORTED_PARAMETER_TYPE,
                getDisplayContextParameter(entity),
                getCaseFieldReference(entity),
                getSheetName(entity)
            ), entity);
    }

    private ValidationError invalidValueError(final T entity) {
        return new SimpleValidationError<>(
            String.format(ERROR_MESSAGE_INVALID_VALUE,
                getDisplayContextParameter(entity),
                getCaseFieldReference(entity),
                getSheetName(entity)
            ), entity);
    }

    private ValidationError unsupportedFieldTypeError(final T entity) {
        return new SimpleValidationError<>(
            String.format(ERROR_MESSAGE_UNSUPPORTED_FIELD_TYPE,
                getDisplayContextParameter(entity),
                getFieldType(entity),
                getCaseFieldReference(entity),
                getSheetName(entity)
            ), entity);
    }
}
