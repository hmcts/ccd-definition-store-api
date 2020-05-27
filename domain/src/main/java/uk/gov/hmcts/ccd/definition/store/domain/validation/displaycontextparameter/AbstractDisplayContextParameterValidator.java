package uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter;

import com.google.common.base.Strings;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameter;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;
import uk.gov.hmcts.ccd.definition.store.domain.validation.SimpleValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

public abstract class AbstractDisplayContextParameterValidator<T extends Serializable> {

    private static final String ERROR_MESSAGE_UNSUPPORTED_PARAMETER_TYPE =
        "Unsupported display context parameter type '%s' for field '%s' on tab '%s'";
    private static final String ERROR_MESSAGE_INVALID_VALUE =
        "Display context parameter '%s' has been incorrectly configured or is invalid for field '%s' on tab '%s'";
    private static final String ERROR_MESSAGE_UNSUPPORTED_FIELD_TYPE =
        "Display context parameter '%s' is unsupported for field type '%s' of field '%s' on tab '%s'";

    private final DisplayContextParameterType[] allowedTypes;
    private final List<String> allowedCollectionFieldTypes;
    private final List<String> allowedFieldTypes;

    private DisplayContextParameterValidatorFactory displayContextParameterValidatorFactory;

    public AbstractDisplayContextParameterValidator(DisplayContextParameterValidatorFactory displayContextParameterValidatorFactory,
                                                    DisplayContextParameterType[] allowedTypes,
                                                    List<String> allowedFieldTypes,
                                                    List<String> allowedCollectionFieldTypes) {
        this.displayContextParameterValidatorFactory = displayContextParameterValidatorFactory;
        this.allowedTypes = allowedTypes;
        this.allowedFieldTypes = allowedFieldTypes;
        this.allowedCollectionFieldTypes = allowedCollectionFieldTypes;
    }

    protected abstract String getDisplayContextParameter(final T entity);

    protected abstract FieldTypeEntity getFieldTypeEntity(final T entity);

    protected abstract String getCaseFieldReference(final T entity);

    protected abstract String getSheetName(final T entity);

    public ValidationResult validate(T entity, List<T> allGenericLayouts) {

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
        if (!allowedFieldTypes.contains(getFieldType(entity)) && !isAllowedCollectionFieldType(entity)) {
            validationResult.addError(unsupportedFieldTypeError(entity));
        }
    }

    private void validateDisplayContextParameter(final T entity, final ValidationResult validationResult) {
        List<DisplayContextParameter> displayContextParameterList =
            DisplayContextParameter.getDisplayContextParametersFor(getDisplayContextParameter(entity));
        if (hasDuplicateTypes(displayContextParameterList)) {
            validationResult.addError(invalidValueError(entity));
            return;
        }

        displayContextParameterList.forEach(displayContextParameter -> {
            if (displayContextParameter.getValue() != null) {
                validateDisplayContextParameterType(displayContextParameter, entity, validationResult);
                if (!validationResult.isValid()) {
                    return;
                }
                validateDisplayContextParameterValue(displayContextParameter, entity, validationResult);
            } else {
                validationResult.addError(invalidValueError(entity));
            }

        });

    }

    protected void validateDisplayContextParameterType(final DisplayContextParameter displayContextParameter,
                                                       final T entity,
                                                       final ValidationResult validationResult) {
        if (Arrays.stream(allowedTypes).noneMatch(displayContextParameter.getType()::equals)) {
            validationResult.addError(unsupportedDisplayContextParameterTypeError(entity));
        }
    }

    protected void validateDisplayContextParameterValue(final DisplayContextParameter displayContextParameter,
                                                        final T entity,
                                                        final ValidationResult validationResult) {
        DisplayContextParameterValidator parameterValidator = displayContextParameterValidatorFactory
            .getValidator(displayContextParameter.getType());
        try {
            parameterValidator.validate(displayContextParameter.getValue(), getFieldType(entity));
        } catch (Exception e) {
            validationResult.addError(invalidValueError(entity));
        }
    }

    private boolean hasDuplicateTypes(List<DisplayContextParameter> displayContextParameters) {
        return displayContextParameters.size() != displayContextParameters.stream()
            .map(DisplayContextParameter::getType)
            .collect(Collectors.toSet())
            .size();
    }

    private String getFieldType(T entity) {
        FieldTypeEntity baseFieldType = getFieldTypeEntity(entity).getBaseFieldType();
        if (baseFieldType != null) {
            return baseFieldType.getReference();
        } else {
            return getFieldTypeEntity(entity).getReference();
        }
    }

    private String getCollectionFieldType(T entity) {
        return getFieldTypeEntity(entity).getCollectionFieldType().getReference();
    }

    private boolean isAllowedCollectionFieldType(T entity) {
        return getFieldType(entity).equals("Collection") && allowedCollectionFieldTypes.contains(getCollectionFieldType(entity));
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
