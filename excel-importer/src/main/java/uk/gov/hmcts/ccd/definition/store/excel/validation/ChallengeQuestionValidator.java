package uk.gov.hmcts.ccd.definition.store.excel.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;
import uk.gov.hmcts.ccd.definition.store.domain.service.question.ChallengeQuestionTabService;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.excel.parser.SpreadsheetParsingException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ChallengeQuestionTabEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Component
public class ChallengeQuestionValidator {

    private List<String> displayContextValues = Arrays.asList(new String[]{DisplayContextParameterType.DATETIMEDISPLAY.toString(), DisplayContextParameterType.DATETIMEENTRY.toString()});
    private ParseContext parseContext;
    private static final String ANSWER_MAIN_SEPARATOR = ",";
    private static final String ANSWER_FIELD_SEPARATOR = "|";
    private static final String ANSWER_FIELD_DOT_SEPARATOR = ".";
    private static final String ANSWER_FIELD_ROLE_SEPARATOR = ":";
    private static final String ANSWER_FIELD_MATCHER = "^\\$\\{\\S.{1,}.\\S.{1,}}$|^\\$\\{\\S.{1,}.\\S.{1,}}:\\[\\S{1,}\\]$";
    private static final String ERROR_MESSAGE = "ChallengeQuestionTab Invalid";
    private final ChallengeQuestionTabService challengeQuestionTabService;

    @Autowired
    public ChallengeQuestionValidator(ChallengeQuestionTabService challengeQuestionTabService) {
        this.challengeQuestionTabService = challengeQuestionTabService;
    }

    public ChallengeQuestionTabEntity validate(ParseContext parseContext, DefinitionDataItem definitionDataItem) {

        this.parseContext = parseContext;
        validateQuestionId(definitionDataItem);
        final String questionId = definitionDataItem.getString(ColumnName.CHALLENGE_QUESTION_QUESTION_ID);
        Optional<ChallengeQuestionTabEntity> result = challengeQuestionTabService.findByQuestionId(questionId);
        final ChallengeQuestionTabEntity challengeQuestionTabEntity = result.orElseGet(ChallengeQuestionTabEntity::new);
        challengeQuestionTabEntity.setQuestionId(questionId);
        challengeQuestionTabEntity.setCaseType(getCaseTypeEntity(definitionDataItem));
        challengeQuestionTabEntity.setAnswerFieldType(getFieldTypeEntity(definitionDataItem));
        validateDisplayOrder(definitionDataItem, challengeQuestionTabEntity);
        validateQuestionText(definitionDataItem, challengeQuestionTabEntity);
        validateDisplayContext(definitionDataItem, challengeQuestionTabEntity);
        validateID(definitionDataItem, challengeQuestionTabEntity);
        validateAnswer(definitionDataItem, challengeQuestionTabEntity);
        return challengeQuestionTabEntity;
    }

    private void validateQuestionId(DefinitionDataItem definitionDataItem) {
        final String questionId = definitionDataItem.getString(ColumnName.CHALLENGE_QUESTION_QUESTION_ID);
        validateNullValue(questionId, ERROR_MESSAGE + " value: questionId cannot be null.");

    }

    private void validateAnswer(DefinitionDataItem definitionDataItem, ChallengeQuestionTabEntity challengeQuestionTabEntity) {
        final String answers = definitionDataItem.getString(ColumnName.CHALLENGE_QUESTION_ANSWER_FIELD);
        final InvalidImportException invalidImportException = new InvalidImportException(ERROR_MESSAGE + " value: "
                + answers + " is not a valid " + ColumnName.CHALLENGE_QUESTION_ANSWER_FIELD
                + " value, Please check the expression format and the roles.");

        validateNullValue(answers, ERROR_MESSAGE + " value: answer cannot be null.");
        final String[] answersRules = answers.split(ANSWER_MAIN_SEPARATOR);

        if (answersRules.length > 0 && answersRules.length != 1) {
            Arrays.asList(answersRules).stream().forEach(currentAnswerExpression -> {
                        validateAnswerExpression(definitionDataItem, challengeQuestionTabEntity, invalidImportException, currentAnswerExpression);
                    }
            );
        } else {
            validateAnswerExpression(definitionDataItem, challengeQuestionTabEntity, invalidImportException, answers);
        }
        challengeQuestionTabEntity.setAnswerField(answers);
    }

    private void validateAnswerExpression(DefinitionDataItem definitionDataItem, ChallengeQuestionTabEntity challengeQuestionTabEntity, InvalidImportException invalidImportException, String currentAnswerExpression) {

        final String[] answersFields = currentAnswerExpression.split(Pattern.quote(ANSWER_FIELD_SEPARATOR));
        Arrays.asList(answersFields).stream().forEach(answersField -> {
                    //validate the format.
                    if (!answersField.matches(ANSWER_FIELD_MATCHER)) {
                        throw new InvalidImportException(ERROR_MESSAGE + " value: "
                                + answersField + " is not a valid " + ColumnName.CHALLENGE_QUESTION_ANSWER_FIELD
                                + ", Please check the expression format and the roles.");
                    }
                    //validate the roles.
                    if (answersField.contains(ANSWER_FIELD_ROLE_SEPARATOR)) {
                        final String role = answersField.split(ANSWER_FIELD_ROLE_SEPARATOR)[1];
                        Optional<UserRoleEntity> result = this.parseContext.getRole(challengeQuestionTabEntity.getCaseType().getReference(), role);
                        result.orElseThrow(() -> new InvalidImportException(ERROR_MESSAGE + " value: "
                                + answersField + " is not a valid " + ColumnName.CHALLENGE_QUESTION_ANSWER_FIELD
                                + " value. Please check the expression format and the roles."));
                    }
                    // validate dot notation content.
                    validateAnswerFieldExpression(definitionDataItem.getString(ColumnName.CASE_TYPE_ID), answersField);
                }
        );

    }

    private void validateAnswerFieldExpression(String currentCaseType, String expression) {
        final InvalidImportException invalidImportException = new InvalidImportException(ERROR_MESSAGE + " value: "
                + expression + " is not a valid " + ColumnName.CHALLENGE_QUESTION_ANSWER_FIELD
                + " value, The expression dot notation values should be valid caseTypes fields.");
        // remove previous validated ${} notation
        final String dotNotationExpression = expression.replace("$", "")
                .replace("{", "")
                .replace("}", "");

        final String[] splittedDotNotationExpression = dotNotationExpression.split(Pattern.quote(ANSWER_FIELD_DOT_SEPARATOR));

        try {
            final FieldTypeEntity fieldType = parseContext.getCaseFieldType(currentCaseType, splittedDotNotationExpression[0]);
            final String[] attributesDotNotation = Arrays.copyOfRange(splittedDotNotationExpression, 1, splittedDotNotationExpression.length);
            Arrays.asList(attributesDotNotation).stream().forEach(attribute -> {
                // Remove Role is needed.
                if (attribute.contains(ANSWER_FIELD_ROLE_SEPARATOR)) {
                    attribute = attribute.substring(0, attribute.indexOf(":"));
                }
                validateAttributes(attribute, fieldType.getComplexFields());
            });
        } catch (SpreadsheetParsingException exception) {
            throw invalidImportException;
        }
    }

    private void validateAttributes(String currentAttribute, List<ComplexFieldEntity> complexFieldACLEntity) {

        Optional<ComplexFieldEntity> result = complexFieldACLEntity.stream().filter(complexFieldACLEItem ->
                complexFieldACLEItem.getReference().equals(currentAttribute)
        ).findAny();
        result.orElseThrow(() -> new InvalidImportException(ERROR_MESSAGE + " value: "
                + currentAttribute + " is not a valid " + ColumnName.CHALLENGE_QUESTION_ANSWER_FIELD
                + " value, The expression dot notation values should be valid caseTypes fields."));
    }

    private void validateDisplayContext(DefinitionDataItem definitionDataItem, ChallengeQuestionTabEntity challengeQuestionTabEntity) {
        final String displayContext = definitionDataItem.getString(ColumnName.DISPLAY_CONTEXT_PARAMETER);
        final InvalidImportException invalidImportException = new InvalidImportException(ERROR_MESSAGE + " value: " + displayContext +
                " is not a valid DisplayContextParameter value. OR Date and Time are the only valid DisplayContextParameter values.");
        final Optional<DisplayContextParameterType> displayContextParameterType = DisplayContextParameterType.getParameterTypeFor(displayContext);
        displayContextParameterType.orElseThrow(() -> invalidImportException);

        if (displayContext != null && displayContextValues.contains(displayContextParameterType.get().name())) {
            challengeQuestionTabEntity.setDisplayContextParameter(displayContext);
        } else {
            throw invalidImportException;
        }
    }

    private void validateQuestionText(DefinitionDataItem definitionDataItem, ChallengeQuestionTabEntity challengeQuestionTabEntity) {
        final String questionText = definitionDataItem.getString(ColumnName.CHALLENGE_QUESTION_TEXT);
        validateNullValue(questionText, ERROR_MESSAGE + " value: QuestionText cannot be null.");
        challengeQuestionTabEntity.setQuestionText(questionText);
    }

    private void validateID(DefinitionDataItem definitionDataItem, ChallengeQuestionTabEntity challengeQuestionTabEntity) {
        final String id = definitionDataItem.getString(ColumnName.ID);
        validateNullValue(id, ERROR_MESSAGE + " value: ID cannot be null.");
        challengeQuestionTabEntity.setChallengeQuestionId(id);
    }

    private void validateDisplayOrder(DefinitionDataItem definitionDataItem, ChallengeQuestionTabEntity challengeQuestionTabEntity) {
        final String displayOrder = definitionDataItem.getString(ColumnName.CHALLENGE_QUESTION_DISPLAY_ORDER);
        try {
            challengeQuestionTabEntity.setOrder(Integer.parseInt(displayOrder));
        } catch (NumberFormatException NumberFormatException) {
            throw new InvalidImportException(ERROR_MESSAGE + " value: " + displayOrder + " is not a valid DisplayOrder.");
        }
    }

    private CaseTypeEntity getCaseTypeEntity(DefinitionDataItem questionItem) {

        final String caseType = questionItem.getString(ColumnName.CASE_TYPE_ID);
        Optional<CaseTypeEntity> caseTypeEntityOptional = parseContext.getCaseTypes().stream().filter(
                caseTypeEntity -> caseTypeEntity.getReference().equals(caseType)
        ).findAny();
        return caseTypeEntityOptional.orElseThrow(() ->
                new InvalidImportException(ERROR_MESSAGE + " Case Type value: " + caseType + "it cannot be found the spreadsheet.")
        );
    }

    private FieldTypeEntity getFieldTypeEntity(DefinitionDataItem questionItem) {

        final String fieldType = questionItem.getString(ColumnName.CHALLENGE_QUESTION_ANSWER_FIELD_TYPE);
        Optional<FieldTypeEntity> fieldTypeEntity = parseContext.getType(fieldType);

        return fieldTypeEntity.orElseThrow(() -> new InvalidImportException(ERROR_MESSAGE + "Field Type value: " + fieldType + " cannot be found as a valid Field type."));
    }

    private void validateNullValue(String value, String message) {
        if (value == null) {
            throw new InvalidImportException(message);
        }
    }

}
