package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import com.google.common.base.Strings;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameter;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.AbstractDisplayContextParameterValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.DisplayContextParameterValidatorFactory;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class EventCaseFieldDateTimeDisplayContextParameterValidatorImpl
    extends AbstractDisplayContextParameterValidator<EventCaseFieldEntity>
    implements EventCaseFieldEntityValidator {

    private static final DisplayContextParameterType[] ALLOWED_TYPES =
        {DisplayContextParameterType.DATETIMEDISPLAY, DisplayContextParameterType.DATETIMEENTRY};
    private static final List<String> ALLOWED_FIELD_TYPES =
        Arrays.asList(FieldTypeUtils.BASE_DATE, FieldTypeUtils.BASE_DATE_TIME);
    private static final Map<DisplayContext, DisplayContextParameterType> DISPLAY_CONTEXT_PARAMETER_TYPE_MAP =
        new EnumMap<>(DisplayContext.class);

    static {
        DISPLAY_CONTEXT_PARAMETER_TYPE_MAP.put(DisplayContext.READONLY, DisplayContextParameterType.DATETIMEDISPLAY);
        DISPLAY_CONTEXT_PARAMETER_TYPE_MAP.put(DisplayContext.OPTIONAL, DisplayContextParameterType.DATETIMEENTRY);
        DISPLAY_CONTEXT_PARAMETER_TYPE_MAP.put(DisplayContext.MANDATORY, DisplayContextParameterType.DATETIMEENTRY);
    }

    public EventCaseFieldDateTimeDisplayContextParameterValidatorImpl(
        DisplayContextParameterValidatorFactory displayContextParameterValidatorFactory) {
        super(displayContextParameterValidatorFactory, ALLOWED_TYPES, ALLOWED_FIELD_TYPES, ALLOWED_FIELD_TYPES);
    }

    @Override
    public ValidationResult validate(EventCaseFieldEntity entity,
                                     EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext) {
        return shouldSkipValidatorForEntity(entity) ? new ValidationResult()
            : super.validate(entity, Collections.emptyList());
    }

    @Override
    protected void validateDisplayContextParameterType(final DisplayContextParameter displayContextParameter,
                                                       final EventCaseFieldEntity entity,
                                                       final ValidationResult validationResult) {
        if (!isSupportedForEventCaseField(displayContextParameter, entity)) {
            validationResult.addError(unsupportedDisplayContextParameterTypeError(entity));
        }
        super.validateDisplayContextParameterType(displayContextParameter, entity, validationResult);
    }

    @Override
    protected String getDisplayContextParameter(EventCaseFieldEntity entity) {
        return entity.getDisplayContextParameter();
    }

    @Override
    protected FieldTypeEntity getFieldTypeEntity(EventCaseFieldEntity entity) {
        return entity.getCaseField().getFieldType();
    }

    private boolean shouldSkipValidatorForEntity(EventCaseFieldEntity entity) {
        final String displayContextParameter = getDisplayContextParameter(entity);
        if (!Strings.isNullOrEmpty(displayContextParameter)) {
            final Optional<DisplayContextParameterType> parameterType =
                DisplayContextParameterType.getParameterTypeFor(displayContextParameter);
            // Validation for #TABLE and #LIST covered in EventCaseFieldDisplayContextParameterValidatorImpl
            return parameterType
                .map(t -> !(t.equals(DisplayContextParameterType.DATETIMEDISPLAY)
                    || t.equals(DisplayContextParameterType.DATETIMEENTRY)))
                .orElse(true);
        }
        return true;
    }

    @Override
    protected String getCaseFieldReference(final EventCaseFieldEntity entity) {
        return (entity.getCaseField() != null ? entity.getCaseField().getReference() : "");
    }

    @Override
    protected String getSheetName(EventCaseFieldEntity entity) {
        return "CaseEventToFields";
    }

    private boolean isSupportedForEventCaseField(final DisplayContextParameter displayContextParameter,
                                                 final EventCaseFieldEntity entity) {
        return DISPLAY_CONTEXT_PARAMETER_TYPE_MAP.containsKey(entity.getDisplayContext())
            && DISPLAY_CONTEXT_PARAMETER_TYPE_MAP.get(entity.getDisplayContext()) == displayContextParameter.getType();
    }
}
