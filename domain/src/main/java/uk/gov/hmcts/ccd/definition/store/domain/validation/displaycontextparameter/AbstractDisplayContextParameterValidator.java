package uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter;

import com.google.common.base.Strings;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameter;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;
import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;

import java.io.Serializable;
import java.util.*;

public abstract class AbstractDisplayContextParameterValidator<T extends Serializable> {

    private static final String ERROR_MESSAGE_UNSUPPORTED_PARAMETER_TYPE =
        "Unsupported display context parameter type '%s' for field '%s' on tab '%s'";
    private static final String ERROR_MESSAGE_INVALID_VALUE =
        "Display context parameter '%s' has been incorrectly configured or is invalid for field '%s' on tab '%s'";
    private static final String ERROR_MESSAGE_UNSUPPORTED_FIELD_TYPE =
        "Display context parameter '%s' is unsupported for field type '%s' of field '%s' on tab '%s'";

    private final DisplayContextParameterType[] ALLOWED_TYPES;
    private final List<String> ALLOWED_FIELD_TYPES;

    private DisplayContextParameterValidatorFactory displayContextParameterValidatorFactory;

    public AbstractDisplayContextParameterValidator(DisplayContextParameterValidatorFactory displayContextParameterValidatorFactory,
                                                    DisplayContextParameterType[] allowedTypes,
                                                    List<String> allowedFieldTypes) {
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

        if (!ALLOWED_FIELD_TYPES.contains(fieldType)) {
            validationResult.addError(unsupportedFieldTypeError(entity));
        }
    }

    private void validateDisplayContextParameter(final T entity, final ValidationResult validationResult) {
        List<DisplayContextParameter> displayContextParameterList =
            DisplayContextParameter.getDisplayContextParameterFor(getDisplayContextParameter(entity));
        displayContextParameterList.forEach(displayContextParameter -> {
            if (displayContextParameter.getValue() != null) {
                validateDisplayContextParameterType(displayContextParameter, entity, validationResult);
                if (!validationResult.isValid()) {
                    return;
                }
                validateDisplayContextParameterValue(displayContextParameter, entity, validationResult);
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

        });

    }

    protected void validateDisplayContextParameterType(final DisplayContextParameter displayContextParameter,
                                                     final T entity,
                                                     final ValidationResult validationResult) {
        if (Arrays.stream(ALLOWED_TYPES).noneMatch(displayContextParameter.getType()::equals)) {
            validationResult.addError(unsupportedDisplayContextParameterTypeError(entity));
        }
    }

    protected void validateDisplayContextParameterValue(final DisplayContextParameter displayContextParameter,
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

    protected ValidationError unsupportedDisplayContextParameterTypeError(final T entity) {
        return new SimpleValidationError<>(
            String.format(ERROR_MESSAGE_UNSUPPORTED_PARAMETER_TYPE,
                getDisplayContextParameter(entity),
                getCaseFieldReference(entity),
                getSheetName(entity)
            ), entity);
    }

    protected ValidationError invalidValueError(final T entity) {
        return new SimpleValidationError<>(
            String.format(ERROR_MESSAGE_INVALID_VALUE,
                getDisplayContextParameter(entity),
                getCaseFieldReference(entity),
                getSheetName(entity)
            ), entity);
    }

    protected ValidationError unsupportedFieldTypeError(final T entity) {
        return new SimpleValidationError<>(
            String.format(ERROR_MESSAGE_UNSUPPORTED_FIELD_TYPE,
                getDisplayContextParameter(entity),
                getFieldType(entity),
                getCaseFieldReference(entity),
                getSheetName(entity)
            ), entity);
    }
}
