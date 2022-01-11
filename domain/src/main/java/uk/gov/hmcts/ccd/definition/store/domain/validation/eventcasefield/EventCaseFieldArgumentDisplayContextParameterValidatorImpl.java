package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameter;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.AbstractDisplayContextParameterValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.DisplayContextParameterValidatorFactory;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.*;

@Component
public class EventCaseFieldArgumentDisplayContextParameterValidatorImpl
    extends AbstractDisplayContextParameterValidator<EventCaseFieldEntity>
    implements EventCaseFieldEntityValidator {

    private static final DisplayContextParameterType[] ALLOWED_TYPES =
        {DisplayContextParameterType.ARGUMENT};
    private static final List<String> ALLOWED_FIELD_TYPES = null;
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
        return super.validate(entity, Collections.emptyList());
    }

    @Override
    protected void validateDisplayContextParameterType(final DisplayContextParameter displayContextParameter,
                                                       final EventCaseFieldEntity entity,
                                                       final ValidationResult validationResult) {
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

    @Override
    protected String getCaseFieldReference(final EventCaseFieldEntity entity) {
        return (entity.getCaseField() != null ? entity.getCaseField().getReference() : "");
    }

    @Override
    protected String getSheetName(EventCaseFieldEntity entity) {
        return "CaseEventToFields";
    }

}
