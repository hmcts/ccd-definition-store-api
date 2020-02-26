package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefieldcomplextype;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameter;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.AbstractDisplayContextParameterValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.DisplayContextParameterValidatorFactory;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventComplexTypeEntity;

import java.util.Optional;

@Component
public class EventComplexTypeEntityDisplayContextParameterValidatorImpl extends AbstractDisplayContextParameterValidator<EventComplexTypeEntity> implements EventComplexTypeEntityValidator {

    private static final DisplayContextParameterType[] ALLOWED_TYPES =
        {DisplayContextParameterType.DATETIMEDISPLAY, DisplayContextParameterType.DATETIMEENTRY};
    private static final String[] ALLOWED_FIELD_TYPES =
        {FieldTypeUtils.BASE_DATE, FieldTypeUtils.BASE_DATE_TIME};

    @Autowired
    public EventComplexTypeEntityDisplayContextParameterValidatorImpl(final DisplayContextParameterValidatorFactory displayContextParameterValidatorFactory) {
        super(displayContextParameterValidatorFactory, ALLOWED_TYPES, ALLOWED_FIELD_TYPES);
    }

    @Override
    public ValidationResult validate(EventComplexTypeEntity eventCaseFieldEntity, EventCaseFieldEntityValidationContext eventCaseFieldEntityValidationContext) {
        return validate(eventCaseFieldEntity);
    }

    @Override
    protected void validateDisplayContextParameterType(final DisplayContextParameter displayContextParameter,
                                                       final EventComplexTypeEntity entity,
                                                       final ValidationResult validationResult) {
        if (entity.getDisplayContext() == DisplayContext.READONLY &&
            displayContextParameter.getType() == DisplayContextParameterType.DATETIMEENTRY) {
            validationResult.addError(unsupportedDisplayContextParameterTypeError(entity));
        }
        super.validateDisplayContextParameterType(displayContextParameter, entity, validationResult);
    }

    @Override
    protected String getDisplayContextParameter(final EventComplexTypeEntity entity) {
        return entity.getDisplayContextParameter();
    }

    @Override
    protected String getFieldType(final EventComplexTypeEntity entity) {
        String reference = entity.getReference();
        Optional<ComplexFieldEntity> complexFieldEntity = entity.getComplexFieldType()
            .getCaseField()
            .getFieldType()
            .getCollectionFieldType()
            .getComplexFields()
            .stream()
            .filter(e -> e.getReference().equals(reference)).findAny();
        return complexFieldEntity.map(ComplexFieldEntity::getBaseTypeString).orElse("");
    }

    protected String getCaseFieldReference(final EventComplexTypeEntity entity) {
        return (entity.getComplexFieldType().getCaseField() != null ? entity.getComplexFieldType().getCaseField().getReference() : "");
    }

    @Override
    protected String getSheetName(EventComplexTypeEntity entity) {
        return "CaseEventToComplexTypes";
    }
}
