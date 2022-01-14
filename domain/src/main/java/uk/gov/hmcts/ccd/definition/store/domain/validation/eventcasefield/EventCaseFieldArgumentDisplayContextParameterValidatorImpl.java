package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import com.google.common.base.Strings;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameter;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.AbstractDisplayContextParameterValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.DisplayContextParameterValidatorFactory;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class EventCaseFieldArgumentDisplayContextParameterValidatorImpl
    extends AbstractDisplayContextParameterValidator<EventCaseFieldEntity>
    implements EventCaseFieldEntityValidator {

    private static final DisplayContextParameterType[] ALLOWED_TYPES =
        {DisplayContextParameterType.ARGUMENT};
    private static final List<String> ALLOWED_FIELD_TYPES = Collections.emptyList();
    private static final Map<DisplayContext, DisplayContextParameterType> DISPLAY_CONTEXT_PARAMETER_TYPE_MAP =
        new EnumMap<>(DisplayContext.class);

    static {
        DISPLAY_CONTEXT_PARAMETER_TYPE_MAP.put(DisplayContext.OPTIONAL, DisplayContextParameterType.ARGUMENT);
    }

    public EventCaseFieldArgumentDisplayContextParameterValidatorImpl(
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
        super.validateDisplayContextParameterType(displayContextParameter, entity, validationResult);
    }

    private boolean shouldSkipValidatorForEntity(EventCaseFieldEntity entity) {
        final String displayContextParameter = getDisplayContextParameter(entity);
        if (!Strings.isNullOrEmpty(displayContextParameter)) {
            final Optional<DisplayContextParameterType> parameterType =
                DisplayContextParameterType.getParameterTypeFor(displayContextParameter);
            return parameterType
                .map(t -> !(t.equals(DisplayContextParameterType.ARGUMENT)))
                .orElse(true);
        }
        return true;
    }

    @Override
    protected String getDisplayContextParameter(EventCaseFieldEntity entity) {
        return entity.getDisplayContextParameter();
    }

    @Override
    protected FieldTypeEntity getFieldTypeEntity(EventCaseFieldEntity entity) {
        return entity.getCaseField().getFieldType();
    }

    @Override
    protected String getCaseFieldReference(final EventCaseFieldEntity entity) {
        return (entity.getCaseField() != null ? entity.getCaseField().getReference() : "");
    }

    @Override
    protected String getSheetName(EventCaseFieldEntity entity) {
        return "CaseEventToFields";
    }

}
