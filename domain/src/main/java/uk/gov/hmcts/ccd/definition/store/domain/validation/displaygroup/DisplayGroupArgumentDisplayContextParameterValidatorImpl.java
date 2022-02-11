package uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup;

import com.google.common.base.Strings;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.AbstractDisplayContextParameterValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.DisplayContextParameterValidatorFactory;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class DisplayGroupArgumentDisplayContextParameterValidatorImpl
    extends AbstractDisplayContextParameterValidator<DisplayGroupCaseFieldEntity>
    implements DisplayGroupCaseFieldValidator {

    private static final DisplayContextParameterType[] ALLOWED_TYPES =
        {DisplayContextParameterType.ARGUMENT};
    private static final List<String> ALLOWED_FIELD_TYPES = Collections.emptyList();

    public DisplayGroupArgumentDisplayContextParameterValidatorImpl(DisplayContextParameterValidatorFactory
                                                                        displayContextParameterValidatorFactory) {
        super(displayContextParameterValidatorFactory, ALLOWED_TYPES, ALLOWED_FIELD_TYPES, ALLOWED_FIELD_TYPES);
    }

    @Override
    protected String getDisplayContextParameter(DisplayGroupCaseFieldEntity entity) {
        return entity.getDisplayContextParameter();
    }

    @Override
    protected FieldTypeEntity getFieldTypeEntity(DisplayGroupCaseFieldEntity entity) {
        return entity.getCaseField().getFieldType();
    }

    @Override
    protected String getCaseFieldReference(final DisplayGroupCaseFieldEntity entity) {
        return (entity.getCaseField() != null ? entity.getCaseField().getReference() : "");
    }

    @Override
    protected String getSheetName(DisplayGroupCaseFieldEntity entity) {
        return "CaseTypeTab";
    }

    @Override
    public ValidationResult validate(DisplayGroupCaseFieldEntity entity) {
        return shouldSkipValidatorForEntity(entity) ? new ValidationResult()
            : super.validate(entity, Collections.emptyList());
    }

    private boolean shouldSkipValidatorForEntity(DisplayGroupCaseFieldEntity entity) {
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
}
