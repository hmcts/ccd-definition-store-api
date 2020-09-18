package uk.gov.hmcts.ccd.definition.store.domain.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.AbstractDisplayContextParameterValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.DisplayContextParameterValidatorFactory;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ChallengeQuestionTabEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class ChallengeQuestionDisplayContextParameterValidator extends AbstractDisplayContextParameterValidator<ChallengeQuestionTabEntity> {

    private static final DisplayContextParameterType[] ALLOWED_TYPES = {DisplayContextParameterType.DATETIMEENTRY};
    private static final List<String> ALLOWED_FIELD_TYPES =
        Arrays.asList(FieldTypeUtils.BASE_DATE, FieldTypeUtils.BASE_DATE_TIME);
    private static final String CHALLENGE_QUESTION_TAB = "ChallengeQuestion";
    private static final String ERROR_MESSAGE = "ChallengeQuestionTab Invalid";

    @Autowired
    public ChallengeQuestionDisplayContextParameterValidator(final DisplayContextParameterValidatorFactory displayContextParameterValidatorFactory) {
        super(displayContextParameterValidatorFactory, ALLOWED_TYPES, ALLOWED_FIELD_TYPES, null);
    }

    @Override
    protected String getDisplayContextParameter(ChallengeQuestionTabEntity entity) {
        return entity.getDisplayContextParameter();
    }

    @Override
    protected FieldTypeEntity getFieldTypeEntity(ChallengeQuestionTabEntity entity) {
        return entity.getAnswerFieldType();
    }

    @Override
    protected String getCaseFieldReference(ChallengeQuestionTabEntity entity) {
        return entity.getQuestionId();
    }

    @Override
    protected String getSheetName(ChallengeQuestionTabEntity entity) {
        return CHALLENGE_QUESTION_TAB;
    }

    public boolean isAnOllowedType(String reference) {
        return ALLOWED_FIELD_TYPES.contains(reference);
    }

    @Override
    public ValidationResult validate(ChallengeQuestionTabEntity entity, List<ChallengeQuestionTabEntity> allGenericLayouts) {
        final String displayContext = entity.getDisplayContextParameter();
        final String questionReference = entity.getAnswerFieldType().getReference();
        if (!isAnOllowedType(questionReference)) {
            final ValidationResult validationResult = new ValidationResult();
            if (displayContext != null) {
                validationResult.addError(new SimpleValidationError(ERROR_MESSAGE + " value: " + displayContext +
                    " is not a valid DisplayContextParameter value for type: " + questionReference, entity)
                );
            }
            return validationResult;
        }
        return super.validate(entity, Collections.EMPTY_LIST);
    }
}
