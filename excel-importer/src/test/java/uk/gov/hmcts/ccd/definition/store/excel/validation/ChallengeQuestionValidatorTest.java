package uk.gov.hmcts.ccd.definition.store.excel.validation;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ChallengeQuestionDisplayContextParameterValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.excel.challengequestion.BaseChallengeQuestionTest;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ChallengeQuestionTabEntity;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ChallengeQuestionValidatorTest extends BaseChallengeQuestionTest {

    private ParseContext parseContext;

    private ChallengeQuestionValidator challengeQuestionValidator;

    @Mock
    ChallengeQuestionDisplayContextParameterValidator challengeQuestionDisplayContextParameterValidator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        parseContext = new ParseContext();
        ChallengeQuestionTabEntity entity = new ChallengeQuestionTabEntity();
        entity.setQuestionId("questionId");
        when(challengeQuestionDisplayContextParameterValidator.validate(any(),any()))
            .thenReturn(new ValidationResult());
        parseContext = buildParseContext();
        challengeQuestionValidator = new ChallengeQuestionValidator(challengeQuestionDisplayContextParameterValidator);
    }

    @Test
    public void testAnswerFormatForLongExpression() {
        String answer = "${OrganisationField.OrganisationName}|${OrganisationField.OrganisationID}:[CLAIMANT],"
            + "${OrganisationField.OrganisationName}|${OrganisationField.OrganisationID}:[DEFENDANT]";
        challengeQuestionValidator.validate(parseContext,
            Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer,"questionId")));
    }

    @Test
    public void testAnswerFormatForSortExpression() {
        String answer = "${OrganisationField.OrganisationName}|${OrganisationField.OrganisationID}:[CLAIMANT]";
        challengeQuestionValidator.validate(parseContext,
            Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_2, QUESTION_ID, answer,"questionId")));
    }

    @Test
    public void testAnswerFormatForMinimumExpression() {
        String answer = "${OrganisationField}:[CLAIMANT]";
        challengeQuestionValidator.validate(parseContext,
            Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_2, QUESTION_ID, answer,"questionId")));
    }

    @Test
    public void testAnswerFormatForSmallestExpression() {
        String answer = "${OrganisationField.OrganisationID}:[CLAIMANT]";
        challengeQuestionValidator.validate(parseContext,
            Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer,"questionId")));
    }

    @Test(expected = InvalidImportException.class)
    public void failAnswerFormatForSmallestExpression() {
        try {
            String answer = "${OrganisationField}:[CCCCCC]";
            challengeQuestionValidator.validate(parseContext,
                Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                    QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer, "questionId")));
        } catch (Exception exception) {
            assertThat(exception.getMessage(),
                is("ChallengeQuestionTab Invalid value: ${OrganisationField}:[CCCCCC] "
                + "is not a valid Answer, Please check the expression format and the roles."));
            throw exception;

        }
    }

    @Test(expected = InvalidImportException.class)
    public void failAnswerFormatForSmallestForRoleExpression() {
        try {
            String answer = "${XXXX}:[CLAIMANT]";

            challengeQuestionValidator.validate(parseContext,
                Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                    QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer, "questionId")));
        } catch (Exception exception) {
            assertThat(exception.getMessage(), is(
                "ChallengeQuestionTab Invalid value: ${XXXX}:[CLAIMANT] is not a valid Answer, "
                + "Please check the expression format and the roles."));
            throw exception;

        }
    }

    @Test(expected = InvalidImportException.class)
    public void failForCaseTypeValidation() {
        try {

            challengeQuestionValidator.validate(parseContext,
                Lists.newArrayList(buildDefinitionDataItem("incorrectCaseType", FIELD_TYPE, "2",
                    QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, ANSWERD,"questionId")));
        } catch (Exception exception) {
            assertThat(exception.getMessage(), is("ChallengeQuestionTab Invalid Case Type value: "
                + "incorrectCaseType. It cannot be found in the spreadsheet."));
            throw exception;
        }
    }

    @Test(expected = InvalidImportException.class)
    public void failForFieldTypeValidation() {
        try {
            challengeQuestionValidator.validate(parseContext,
                Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, "fiedType-CCC", "2",
                    QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, ANSWERD,"questionId")));
        } catch (Exception exception) {
            assertThat(exception.getMessage(), is("ChallengeQuestionTab InvalidField Type value: "
                + "fiedType-CCC cannot be found as a valid Field type."));
            throw exception;
        }
    }

    @Test(expected = InvalidImportException.class)
    public void failForDisplayOrderValidation() {
        try {
            challengeQuestionValidator.validate(parseContext,
                Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "TTTT",
                    QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, ANSWERD,"questionId")));
        } catch (Exception exception) {
            assertThat(exception.getMessage(),
                is("ChallengeQuestionTab Invalid value: TTTT is not a valid DisplayOrder."));
            throw exception;
        }
    }

    @Test(expected = InvalidImportException.class)
    public void failForQuestionTextValidation() {
        try {
            challengeQuestionValidator.validate(parseContext,
                Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2", null,
                    DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, ANSWERD,"questionId")));
        } catch (Exception exception) {
            assertThat(exception.getMessage(),
                is("ChallengeQuestionTab Invalid value: QuestionText cannot be null."));
            throw exception;
        }
    }

    @Test(expected = InvalidImportException.class)
    public void failForIDValidation() {
        try {
            challengeQuestionValidator.validate(parseContext,
                Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, null, ANSWERD,"questionId")));
        } catch (Exception exception) {
            assertThat(exception.getMessage(), is("ChallengeQuestionTab Invalid value: ID cannot be null."));
            throw exception;
        }
    }

    @Test(expected = InvalidImportException.class)
    public void failAnswerFormatDueToInvalidRole() {
        try {
            String answer = "${OrganisationField.OrganisationName}|${OrganisationField.OrganisationID}:[NO],"
                + "${OrganisationField.OrganisationName}|${OrganisationField.OrganisationID}:[DEFENDANT]";
            challengeQuestionValidator.validate(parseContext,
                Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                    QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer,"questionId")));
        } catch (Exception exception) {
            assertThat(exception.getMessage(), is("ChallengeQuestionTab Invalid value: "
                + "${OrganisationField.OrganisationID}:[NO] is not a valid Answer, "
                + "Please check the expression format and the roles."));
            throw exception;
        }
    }

    @Test(expected = InvalidImportException.class)
    public void failAnswerFormatDueToInvalidPathInExpressionAttribute() {
        try {
            String answer = "${OrganisationField.XXXX}|${OrganisationField.OrganisationID}:[DEFENDANT],"
                + "${OrganisationField.OrganisationName}|${OrganisationField.OrganisationID}:[DEFENDANT]";
            challengeQuestionValidator.validate(parseContext,
                Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                    QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer,"questionId")));
        } catch (Exception exception) {
            assertThat(exception.getMessage(), is("ChallengeQuestionTab Invalid value: ${OrganisationField.XXXX} "
                + "is not a valid Answer value. "
                + "The expression dot notation values should be valid caseTypes fields."));
            throw exception;
        }
    }

    @Test(expected = InvalidImportException.class)
    public void failAnswerFormatDueToInvalidPathInExpression() {
        try {
            String answer = "${XXXXX.OrganisationName}|${OrganisationField.OrganisationID}:[DEFENDANT],"
                + "${OrganisationField.OrganisationName}|${OrganisationField.OrganisationID}:[DEFENDANT]";
            challengeQuestionValidator.validate(parseContext,
                Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                    QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer,"questionId")));
        } catch (Exception exception) {
            exception.printStackTrace();
            assertThat(exception.getMessage(),
                is("ChallengeQuestionTab Invalid value: ${XXXXX.OrganisationName} is not a valid Answer value. "
                    + "The expression dot notation values should be valid caseTypes fields."));
            throw exception;
        }
    }

    @Test(expected = InvalidImportException.class)
    public void failAnswerFormatDueToInvalidFormat() {
        try {
            String answer = "CXFSEKEOE";
            challengeQuestionValidator.validate(parseContext,
                Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                    QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer,"questionId")));
        } catch (Exception exception) {
            assertThat(exception.getMessage(),
                is("ChallengeQuestionTab Invalid value: CXFSEKEOE is not a valid Answer, "
                + "Please check the expression format and the roles."));
            throw exception;
        }
    }


    @Test(expected = InvalidImportException.class)
    public void shouldFailWhenQuestionIdIsNotUniqueWithInCaseType() {
        try {
            String answer = "${OrganisationField.OrganisationID}:[CLAIMANT]";
            challengeQuestionValidator.validate(parseContext,
                Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                    QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer,"questionId"),
                    buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                        QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer,"questionId")));
        } catch (Exception exception) {
            assertThat(exception.getMessage(),
                is("QuestionId cannot be duplicated within case type "
                    + "and challenge question in ChallengeQuestion tab"));
            throw exception;
        }
    }

    @Test(expected = InvalidImportException.class)
    public void shouldFailWhenDisplayOrderIsNotUniqueWithInCaseType() {
        try {
            String answer = "${OrganisationField.OrganisationID}:[CLAIMANT]";
            challengeQuestionValidator.validate(parseContext,
                Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                    QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer,"questionId"),
                    buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                        QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer,"questionId2")));
        } catch (Exception exception) {
            assertThat(exception.getMessage(),
                is("DisplayOrder cannot be duplicated within case type "
                    + "and challenge question in ChallengeQuestion tab"));
            throw exception;
        }
    }
}
