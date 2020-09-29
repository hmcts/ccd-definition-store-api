package uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup;

import com.google.common.base.Strings;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.AbstractDisplayContextParameterValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.DisplayContextParameterValidatorFactory;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class DisplayGroupDateTimeDisplayContextParameterValidatorImpl
    extends AbstractDisplayContextParameterValidator<DisplayGroupCaseFieldEntity>
    implements DisplayGroupCaseFieldValidator {

    private static final DisplayContextParameterType[] ALLOWED_TYPES =
        {DisplayContextParameterType.DATETIMEDISPLAY};
    private static final List<String> ALLOWED_FIELD_TYPES =
        Arrays.asList(FieldTypeUtils.BASE_DATE, FieldTypeUtils.BASE_DATE_TIME);

    public DisplayGroupDateTimeDisplayContextParameterValidatorImpl(
        DisplayContextParameterValidatorFactory displayContextParameterValidatorFactory) {
        super(displayContextParameterValidatorFactory, ALLOWED_TYPES, ALLOWED_FIELD_TYPES, ALLOWED_FIELD_TYPES);
    }

    @Override
    public ValidationResult validate(DisplayGroupCaseFieldEntity entity) {
        return shouldSkipValidatorForEntity(entity) ? new ValidationResult()
            : super.validate(entity, Collections.emptyList());
    }

    @Override
    protected String getDisplayContextParameter(DisplayGroupCaseFieldEntity entity) {
        return entity.getDisplayContextParameter();
    }

    @Override
    protected FieldTypeEntity getFieldTypeEntity(DisplayGroupCaseFieldEntity entity) {
        return entity.getCaseField().getFieldType();
    }

    private boolean shouldSkipValidatorForEntity(DisplayGroupCaseFieldEntity entity) {
        final String displayContextParameter = getDisplayContextParameter(entity);
        if (!Strings.isNullOrEmpty(displayContextParameter)) {
            final Optional<DisplayContextParameterType> parameterType =
                DisplayContextParameterType.getParameterTypeFor(displayContextParameter);
            // Validation for #TABLE and #LIST covered in DisplayGroupDisplayContextParamValidator
            return parameterType
                .map(t -> !(t.equals(DisplayContextParameterType.DATETIMEDISPLAY)
                    || t.equals(DisplayContextParameterType.DATETIMEENTRY)))
                .orElse(true);
        }
        return true;
    }

    @Override
    protected String getCaseFieldReference(final DisplayGroupCaseFieldEntity entity) {
        return (entity.getCaseField() != null ? entity.getCaseField().getReference() : "");
    }

    @Override
    protected String getSheetName(DisplayGroupCaseFieldEntity entity) {
        return "CaseTypeTab";
    }
}
