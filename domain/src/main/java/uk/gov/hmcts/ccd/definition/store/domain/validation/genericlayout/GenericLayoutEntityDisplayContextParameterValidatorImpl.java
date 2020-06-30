package uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameter;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.AbstractDisplayContextParameterValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.DisplayContextParameterValidatorFactory;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;

import java.util.Arrays;
import java.util.List;

@Component
public class GenericLayoutEntityDisplayContextParameterValidatorImpl
    extends AbstractDisplayContextParameterValidator<GenericLayoutEntity>
    implements GenericLayoutValidator {

    private static final DisplayContextParameterType[] ALLOWED_TYPES =
        {DisplayContextParameterType.DATETIMEDISPLAY, DisplayContextParameterType.DATETIMEENTRY};
    private static final List<String> ALLOWED_FIELD_TYPES =
        Arrays.asList(FieldTypeUtils.BASE_DATE, FieldTypeUtils.BASE_DATE_TIME);

    @Autowired
    public GenericLayoutEntityDisplayContextParameterValidatorImpl(final DisplayContextParameterValidatorFactory displayContextParameterValidatorFactory) {
        super(displayContextParameterValidatorFactory, ALLOWED_TYPES, ALLOWED_FIELD_TYPES, ALLOWED_FIELD_TYPES);
    }

    @Override
    protected void validateDisplayContextParameterType(final DisplayContextParameter displayContextParameter,
                                                       final GenericLayoutEntity entity,
                                                       final ValidationResult validationResult) {
        if (isUnsupportedForGenericLayoutEntity(displayContextParameter, entity)) {
            validationResult.addError(unsupportedDisplayContextParameterTypeError(entity));
        }
        super.validateDisplayContextParameterType(displayContextParameter, entity, validationResult);
    }

    @Override
    protected String getDisplayContextParameter(final GenericLayoutEntity entity) {
        return entity.getDisplayContextParameter();
    }

    @Override
    protected FieldTypeEntity getFieldTypeEntity(final GenericLayoutEntity entity) {
        return entity.getCaseField().getFieldType();
    }

    @Override
    protected String getCaseFieldReference(final GenericLayoutEntity entity) {
        return (entity.getCaseField() != null ? entity.getCaseField().getReference() : "");
    }

    @Override
    protected String getSheetName(GenericLayoutEntity entity) {
        return entity.getSheetName();
    }

    private boolean isUnsupportedForGenericLayoutEntity(final DisplayContextParameter displayContextParameter,
                                                        final GenericLayoutEntity entity) {
        return (getSheetName(entity).contains("Input") && displayContextParameter.getType() == DisplayContextParameterType.DATETIMEDISPLAY)
            || (getSheetName(entity).contains("Result") && displayContextParameter.getType() == DisplayContextParameterType.DATETIMEENTRY);
    }
}
