package uk.gov.hmcts.ccd.definition.store.domain.validation;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.AbstractDisplayContextParameterValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.DisplayContextParameterValidatorFactory;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ChallengeQuestionTabEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

@Component
public class ChallengeQuestionDisplayContextParameterValidator
    extends AbstractDisplayContextParameterValidator<ChallengeQuestionTabEntity> {

    private static final DisplayContextParameterType[] ALLOWED_TYPES = {DisplayContextParameterType.DATETIMEENTRY};
    private static final List<String> ALLOWED_FIELD_TYPES =
        Arrays.asList(FieldTypeUtils.BASE_DATE, FieldTypeUtils.BASE_DATE_TIME);
    private static final String CHALLENGE_QUESTION_TAB = "ChallengeQuestion";

    @Autowired
    public ChallengeQuestionDisplayContextParameterValidator(
        final DisplayContextParameterValidatorFactory displayContextParameterValidatorFactory) {
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
}
