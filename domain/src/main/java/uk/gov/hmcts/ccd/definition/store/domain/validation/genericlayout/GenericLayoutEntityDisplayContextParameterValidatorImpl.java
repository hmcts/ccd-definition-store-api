package uk.gov.hmcts.ccd.definition.store.domain.validation.genericlayout;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.AbstractDisplayContextParameterValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.DisplayContextParameterValidatorFactory;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;

@Component
public class GenericLayoutEntityDisplayContextParameterValidatorImpl extends AbstractDisplayContextParameterValidator<GenericLayoutEntity> implements GenericLayoutValidator {

    private static final DisplayContextParameterType[] ALLOWED_TYPES =
        { DisplayContextParameterType.DATETIMEDISPLAY, DisplayContextParameterType.DATETIMEENTRY };
    private static final String[] ALLOWED_FIELD_TYPES =
        { FieldTypeUtils.BASE_DATE, FieldTypeUtils.BASE_DATE_TIME };

    @Autowired
    public GenericLayoutEntityDisplayContextParameterValidatorImpl(final DisplayContextParameterValidatorFactory displayContextParameterValidatorFactory) {
        super(displayContextParameterValidatorFactory, ALLOWED_TYPES, ALLOWED_FIELD_TYPES);
    }

    @Override
    protected String getDisplayContextParameter(final GenericLayoutEntity entity) {
        return entity.getDisplayContextParameter();
    }

    @Override
    protected String getFieldType(final GenericLayoutEntity entity) {
        return entity.getCaseField().getFieldType().getReference();
    }

    @Override
    protected ValidationError unsupportedDisplayContextParameterTypeError(final GenericLayoutEntity entity) {
        return new ValidationError(
            String.format(ERROR_MESSAGE_UNSUPPORTED_PARAMETER_TYPE,
                getDisplayContextParameter(entity),
                getCaseFieldReference(entity)
            ), entity);
    }

    @Override
    protected ValidationError invalidValueError(final GenericLayoutEntity entity) {
        return new ValidationError(
            String.format(ERROR_MESSAGE_INVALID_VALUE,
                getDisplayContextParameter(entity),
                getCaseFieldReference(entity)
            ), entity);
    }

    @Override
    protected ValidationError unsupportedFieldTypeError(final GenericLayoutEntity entity) {
        return new ValidationError(
            String.format(ERROR_MESSAGE_UNSUPPORTED_FIELD_TYPE,
                getDisplayContextParameter(entity),
                getFieldType(entity),
                getCaseFieldReference(entity)
            ), entity);
    }

    private String getCaseFieldReference(final GenericLayoutEntity entity) {
        return (entity.getCaseField() != null ? entity.getCaseField().getReference() : "");
    }
}
