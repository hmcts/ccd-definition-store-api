package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefieldcomplextype;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.AbstractDisplayContextParameterValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.DisplayContextParameterValidatorFactory;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventComplexTypeEntity;

import java.util.List;

@Component
public class EventComplexTypeEntityDisplayContextParameterValidatorImpl extends AbstractDisplayContextParameterValidator<EventComplexTypeEntity> implements EventComplexTypeEntityValidator {

    private static final DisplayContextParameterType[] ALLOWED_TYPES =
        { DisplayContextParameterType.DATETIMEDISPLAY, DisplayContextParameterType.DATETIMEENTRY };
    private static final String[] ALLOWED_FIELD_TYPES =
        { FieldTypeUtils.BASE_DATE, FieldTypeUtils.BASE_DATE_TIME };

    @Autowired
    public EventComplexTypeEntityDisplayContextParameterValidatorImpl(final DisplayContextParameterValidatorFactory displayContextParameterValidatorFactory) {
        super(displayContextParameterValidatorFactory, ALLOWED_TYPES, ALLOWED_FIELD_TYPES);
    }

    @Override
    public ValidationResult validate(EventComplexTypeEntity eventCaseFieldEntity, EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext) {
        return validate(eventCaseFieldEntity);
    }

    @Override
    protected String getDisplayContextParameter(final EventComplexTypeEntity entity) {
        return entity.getDisplayContextParameter();
    }

    @Override
    protected String getFieldType(final EventComplexTypeEntity entity) {
        // Always return allowed field type
        List<ComplexFieldEntity> list = entity.getComplexFieldType().getCaseField().getFieldType().getCollectionFieldType().getComplexFields();
        return ALLOWED_FIELD_TYPES[0];
    }

    @Override
    protected ValidationError unsupportedDisplayContextParameterTypeError(final EventComplexTypeEntity entity) {
        return new ValidationError(
            String.format(ERROR_MESSAGE_UNSUPPORTED_PARAMETER_TYPE,
                getDisplayContextParameter(entity),
                getCaseFieldReference(entity)
            ), entity);
    }

    @Override
    protected ValidationError invalidValueError(final EventComplexTypeEntity entity) {
        return new ValidationError(
            String.format(ERROR_MESSAGE_INVALID_VALUE,
                getDisplayContextParameter(entity),
                getCaseFieldReference(entity)
            ), entity);
    }

    @Override
    protected ValidationError unsupportedFieldTypeError(final EventComplexTypeEntity entity) {
        return new ValidationError(
            String.format(ERROR_MESSAGE_UNSUPPORTED_FIELD_TYPE,
                getDisplayContextParameter(entity),
                getFieldType(entity),
                getCaseFieldReference(entity)
            ), entity);
    }

    private String getCaseFieldReference(final EventComplexTypeEntity entity) {
        return (entity.getComplexFieldType().getCaseField() != null ? entity.getComplexFieldType().getCaseField().getReference() : "");
    }
}
