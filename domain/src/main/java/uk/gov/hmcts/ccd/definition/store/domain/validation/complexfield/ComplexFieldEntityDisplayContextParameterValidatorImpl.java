package uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefieldcomplextype;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameter;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield.*;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.AbstractDisplayContextParameterValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.DisplayContextParameterValidatorFactory;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldEntityValidationContext;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;

import java.util.Optional;

@Component
public class ComplexFieldEntityDisplayContextParameterValidatorImpl extends AbstractDisplayContextParameterValidator<ComplexFieldEntity> implements ComplexFieldValidator {

    private static final DisplayContextParameterType[] ALLOWED_TYPES =
        {DisplayContextParameterType.DATETIMEDISPLAY, DisplayContextParameterType.DATETIMEENTRY};
    private static final String[] ALLOWED_FIELD_TYPES =
        {FieldTypeUtils.BASE_DATE, FieldTypeUtils.BASE_DATE_TIME};

    @Autowired
    public ComplexFieldEntityDisplayContextParameterValidatorImpl(final DisplayContextParameterValidatorFactory displayContextParameterValidatorFactory) {
        super(displayContextParameterValidatorFactory, ALLOWED_TYPES, ALLOWED_FIELD_TYPES);
    }

    @Override
    public ValidationResult validate(ComplexFieldEntity complexFieldEntity, ValidationContext validationContext) {
        return validate(complexFieldEntity);
    }

    @Override
    protected void validateDisplayContextParameterType(final DisplayContextParameter displayContextParameter,
                                                       final ComplexFieldEntity entity,
                                                       final ValidationResult validationResult) {
        if (displayContextParameter.getType() == DisplayContextParameterType.DATETIMEDISPLAY ||
            displayContextParameter.getType() == DisplayContextParameterType.DATETIMEENTRY) {
            validationResult.addError(unsupportedDisplayContextParameterTypeError(entity));
        }
        super.validateDisplayContextParameterType(displayContextParameter, entity, validationResult);
    }

    @Override
    protected String getDisplayContextParameter(final ComplexFieldEntity entity) {
        return entity.getDisplayContextParameter();
    }

    @Override
    protected String getFieldType(final ComplexFieldEntity entity) {
        String reference = entity.getReference();
        Optional<ComplexFieldEntity> complexFieldEntity = entity.getComplexFieldType()
            .getBaseFieldType()
            .getCollectionFieldType()
            .getComplexFields()
            .stream()
            .filter(e -> e.getReference().equals(reference)).findAny();
        return complexFieldEntity.map(ComplexFieldEntity::getBaseTypeString).orElse("");
    }

    protected String getCaseFieldReference(final ComplexFieldEntity entity) {
        return (entity.getComplexFieldType().getBaseFieldType() != null ? entity.getComplexFieldType().getBaseFieldType().getReference() : "");
    }

    @Override
    protected String getSheetName(ComplexFieldEntity entity) {
        return "ComplexTypes";
    }
}
