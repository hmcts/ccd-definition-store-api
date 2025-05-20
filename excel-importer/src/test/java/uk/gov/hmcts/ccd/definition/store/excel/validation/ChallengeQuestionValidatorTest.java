package uk.gov.hmcts.ccd.definition.store.excel.validation;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ChallengeQuestionDisplayContextParameterValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.excel.challengequestion.BaseChallengeQuestionTest;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ChallengeQuestionTabEntity;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ChallengeQuestionValidatorTest extends BaseChallengeQuestionTest {

    private ParseContext parseContext;

    private ChallengeQuestionValidator challengeQuestionValidator;

    @Mock
    ChallengeQuestionDisplayContextParameterValidator challengeQuestionDisplayContextParameterValidator;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        parseContext = new ParseContext();
        ChallengeQuestionTabEntity entity = new ChallengeQuestionTabEntity();
        entity.setQuestionId("questionId");
        when(challengeQuestionDisplayContextParameterValidator.validate(any(), any()))
                .thenReturn(new ValidationResult());
        parseContext = buildParseContext();

        challengeQuestionValidator = new ChallengeQuestionValidator(
                challengeQuestionDisplayContextParameterValidator,
                new DotNotationValidator());
    }

    @Test
    public void testAnswerFormatForLongExpression() {
        String answer = "${OrganisationField.OrganisationName}|${OrganisationField.OrganisationID}:[CLAIMANT],"
                + "${OrganisationField.OrganisationName}|${OrganisationField.OrganisationID}:[DEFENDANT]";
        challengeQuestionValidator.validate(parseContext,
                Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                        QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer, "questionId")));
    }

    @Test
    public void testAnswerFormatForSortExpression() {
        String answer = "${OrganisationField.OrganisationName}|${OrganisationField.OrganisationID}:[CLAIMANT]";
        challengeQuestionValidator.validate(parseContext,
                Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                        QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_2, QUESTION_ID, answer, "questionId")));
    }

    @Test
    public void testAnswerFormatForMinimumExpression() {
        String answer = "${OrganisationField}:[CLAIMANT]";
        challengeQuestionValidator.validate(parseContext,
                Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                        QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_2, QUESTION_ID, answer, "questionId")));
    }

    @Test
    public void testAnswerFormatForSmallestExpression() {
        String answer = "${OrganisationField.OrganisationID}:[CLAIMANT]";
        challengeQuestionValidator.validate(parseContext,
                Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                        QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer, "questionId")));
    }

    @Test
    public void failAnswerFormatForSmallestExpression() {
        InvalidImportException exception = assertThrows(InvalidImportException.class, () -> {
            String answer = "${OrganisationField.OrganisationName}:[CCCCCC]";
            challengeQuestionValidator.validate(parseContext,
                    Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                            QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer, "questionId")));
        });
        assertThat(exception.getMessage(),
                is("ChallengeQuestionTab Invalid value: ${OrganisationField.OrganisationName}:[CCCCCC] "
                        + "is not a valid Answer, Please check the expression format and the roles."));
    }

    @Test
    public void failAnswerFormatForSmallestForRoleExpression() {
        InvalidImportException exception = assertThrows(InvalidImportException.class, () -> {
            String answer = "${XXXX}:[CLAIMANT]";
            challengeQuestionValidator.validate(parseContext,
                    Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                            QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer, "questionId")));
        });
        assertThat(exception.getMessage(),
                is("ChallengeQuestionTab Invalid value: ${XXXX}:[CLAIMANT] is not a valid Answer, "
                        + "Please check the expression format and the roles."));
    }

    @Test
    public void failForCaseTypeValidation() {
        InvalidImportException exception = assertThrows(InvalidImportException.class, () -> {
            challengeQuestionValidator.validate(parseContext,
                    Lists.newArrayList(buildDefinitionDataItem("incorrectCaseType", FIELD_TYPE, "2",
                            QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, ANSWERD, "questionId")));
        });
        assertThat(exception.getMessage(), is("ChallengeQuestionTab Invalid Case Type value: "
                + "incorrectCaseType. It cannot be found in the spreadsheet."));
    }

    @Test
    public void failForFieldTypeValidation() {
        InvalidImportException exception = assertThrows(InvalidImportException.class, () -> {
            challengeQuestionValidator.validate(parseContext,
                    Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, "fiedType-CCC", "2",
                            QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, ANSWERD, "questionId")));
        });
        assertThat(exception.getMessage(), is("ChallengeQuestionTab InvalidField Type value: "
                + "fiedType-CCC cannot be found as a valid Field type."));
    }

    @Test
    public void failForDisplayOrderValidation() {
        InvalidImportException exception = assertThrows(InvalidImportException.class, () -> {
            challengeQuestionValidator.validate(parseContext,
                    Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "TTTT",
                            QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, ANSWERD, "questionId")));
        });
        assertThat(exception.getMessage(),
                is("ChallengeQuestionTab Invalid value: TTTT is not a valid DisplayOrder."));
    }

    @Test
    public void failForQuestionTextValidation() {

        InvalidImportException exception = assertThrows(InvalidImportException.class, () -> {
            challengeQuestionValidator.validate(parseContext,
                    Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                            null, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, ANSWERD, "questionId")));
        });
        assertThat(exception.getMessage(),
                is("ChallengeQuestionTab Invalid value: QuestionText cannot be null."));
    }

    @Test
    public void failForIDValidation() {
        InvalidImportException exception = assertThrows(InvalidImportException.class, () -> {
            challengeQuestionValidator.validate(parseContext,
                    Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                            QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, null, ANSWERD, "questionId")));
        });
        assertThat(exception.getMessage(),
                is("ChallengeQuestionTab Invalid value: ID cannot be null."));
    }

    @Test
    public void failAnswerFormatDueToInvalidRole() {
        InvalidImportException exception = assertThrows(InvalidImportException.class, () -> {
            String answer = "${OrganisationField.OrganisationName}|${OrganisationField.OrganisationID}:[NO],"
                    + "${OrganisationField.OrganisationName}|${OrganisationField.OrganisationID}:[DEFENDANT]";
            challengeQuestionValidator.validate(parseContext,
                    Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                            QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer, "questionId")));
        });
        assertThat(exception.getMessage(), is("ChallengeQuestionTab Invalid value: "
                + "${OrganisationField.OrganisationID}:[NO] is not a valid Answer, "
                + "Please check the expression format and the roles."));
    }

    @Test
    public void failAnswerFormatDueToInvalidPathInExpressionAttribute() {
        InvalidImportException exception = assertThrows(InvalidImportException.class, () -> {
            String answer = "${OrganisationField.XXXX}|${OrganisationField.OrganisationID}:[DEFENDANT],"
                    + "${OrganisationField.OrganisationName}|${OrganisationField.OrganisationID}:[DEFENDANT]";
            challengeQuestionValidator.validate(parseContext,
                    Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                            QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer, "questionId")));
        });

        assertThat(exception.getMessage(), is("ChallengeQuestionTab Invalid value: ${OrganisationField.XXXX} "
                + "is not a valid Answer value. "
                + "The expression dot notation values should be valid caseTypes fields."));
    }

    @Test
    public void failAnswerFormatDueToInvalidPathInExpression() {
        InvalidImportException exception = assertThrows(InvalidImportException.class, () -> {
            String answer = "${XXXXX.OrganisationName}|${OrganisationField.OrganisationID}:[DEFENDANT],"
                    + "${OrganisationField.OrganisationName}|${OrganisationField.OrganisationID}:[DEFENDANT]";
            challengeQuestionValidator.validate(parseContext,
                    Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                            QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer, "questionId")));
        });
        assertThat(exception.getMessage(),
                is("ChallengeQuestionTab Invalid value: ${XXXXX.OrganisationName} is not a valid Answer value. "
                        + "The expression dot notation values should be valid caseTypes fields."));
    }

    @Test
    public void failAnswerFormatDueToInvalidFormat() {
        InvalidImportException exception = assertThrows(InvalidImportException.class, () -> {
            String answer = "CXFSEKEOE";
            challengeQuestionValidator.validate(parseContext,
                    Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                            QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer, "questionId")));
        });
        assertThat(exception.getMessage(),
                is("ChallengeQuestionTab Invalid value: CXFSEKEOE is not a valid Answer, "
                        + "Please check the expression format and the roles."));
    }

    @Test
    public void shouldFailWhenQuestionIdIsNotUniqueWithInCaseType() {
        InvalidImportException exception = assertThrows(InvalidImportException.class, () -> {
            String answer = "${OrganisationField.OrganisationID}:[CLAIMANT]";
            challengeQuestionValidator.validate(parseContext,
                    Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                            QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer, "questionId"),
                            buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                                    QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer, "questionId")));
        });
        assertThat(exception.getMessage(),
                is("QuestionId cannot be duplicated within case type "
                        + "and challenge question in ChallengeQuestion tab"));
    }

    @Test
    public void shouldFailWhenDisplayOrderIsNotUniqueWithInCaseType() {
        InvalidImportException exception = assertThrows(InvalidImportException.class, () -> {
            String answer = "${OrganisationField.OrganisationID}:[CLAIMANT]";
            challengeQuestionValidator.validate(parseContext,
                    Lists.newArrayList(buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                            QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer, "questionId"),
                            buildDefinitionDataItem(CASE_TYPE, FIELD_TYPE, "2",
                                    QUESTION_TEXT, DISPLAY_CONTEXT_PARAMETER_1, QUESTION_ID, answer, "questionId2")));
        });
        assertThat(exception.getMessage(),
                is("DisplayOrder cannot be duplicated within case type "
                        + "and challenge question in ChallengeQuestion tab"));
    }
}
