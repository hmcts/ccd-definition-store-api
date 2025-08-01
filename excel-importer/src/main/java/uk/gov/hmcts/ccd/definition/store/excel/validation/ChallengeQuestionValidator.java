package uk.gov.hmcts.ccd.definition.store.excel.validation;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ChallengeQuestionDisplayContextParameterValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessProfileEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ChallengeQuestionTabEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.stream.Collectors.groupingBy;

@Component
public class ChallengeQuestionValidator {

    private ParseContext parseContext;
    private static final String ANSWER_MAIN_SEPARATOR = ",";
    private static final String ANSWER_FIELD_SEPARATOR = "|";
    private static final String ANSWER_FIELD_ROLE_SEPARATOR = ":";
    private static final String ANSWER_FIELD_MATCHER
        = "^\\$\\{\\S.{1,}.\\S.{1,}}$|^\\$\\{\\S.{1,}.\\S.{1,}}:\\[\\S{1,}\\]$";
    private static final String ERROR_MESSAGE = "ChallengeQuestionTab Invalid";
    private static final String NOT_VALID = " is not a valid ";


    private final ChallengeQuestionDisplayContextParameterValidator challengeQuestionDisplayContextParameterValidator;
    private final DotNotationValidator dotNotationValidator;

    @Autowired
    public ChallengeQuestionValidator(
        ChallengeQuestionDisplayContextParameterValidator challengeQuestionDisplayContextParameterValidator,
        DotNotationValidator dotNotationValidator) {

        this.challengeQuestionDisplayContextParameterValidator = challengeQuestionDisplayContextParameterValidator;
        this.dotNotationValidator = dotNotationValidator;
    }

    public void validateDisplayContext(ChallengeQuestionTabEntity challengeQuestionTabEntity) {
        final ValidationResult validationResult = challengeQuestionDisplayContextParameterValidator.validate(
            challengeQuestionTabEntity,
            Collections.emptyList()
        );
        if (!validationResult.isValid()) {
            throw new ValidationException(validationResult);
        }
    }

    public void validate(ParseContext parseContext, List<DefinitionDataItem> questionItems) {
        this.parseContext = parseContext;
        validateUniqueIds(questionItems);
        validateUniqueDisplayOrder(questionItems);
        questionItems.forEach(this::validate);
    }

    private void validate(DefinitionDataItem definitionDataItem) {
        validateQuestionId(definitionDataItem);
        validateID(definitionDataItem);
        validateFieldType(definitionDataItem);
        validateDisplayOrder(definitionDataItem);
        validateQuestionText(definitionDataItem);
        CaseTypeEntity caseTypeEntity = validateCaseType(definitionDataItem);
        validateAnswer(definitionDataItem, caseTypeEntity);
    }

    private void validateUniqueIds(List<DefinitionDataItem> questionItems) {
        Map<Triple<String, String, String>, List<DefinitionDataItem>> caseTypeQuestionItems =
            questionItems
                .stream()
                .collect(groupingBy(p ->
                    Triple.of(p.getString(ColumnName.CASE_TYPE_ID),
                        p.getString(ColumnName.ID),
                        p.getString(ColumnName.CHALLENGE_QUESTION_QUESTION_ID))));

        caseTypeQuestionItems.keySet()
            .forEach(triple -> {
                if (caseTypeQuestionItems.get(triple).size() > 1) {
                    throw new InvalidImportException("QuestionId cannot be duplicated within case type "
                        + "and challenge question in ChallengeQuestion tab");
                }
            });
    }

    private void validateUniqueDisplayOrder(List<DefinitionDataItem> questionItems) {
        Map<Triple<String, String, String>, List<DefinitionDataItem>> challengeQuestionDisplayOrder =
            questionItems
                .stream()
                .collect(groupingBy(p ->
                    Triple.of(p.getString(ColumnName.CASE_TYPE_ID),
                        p.getString(ColumnName.ID),
                        p.getString(ColumnName.DISPLAY_ORDER))));

        challengeQuestionDisplayOrder.keySet()
            .forEach(triple -> {
                if (challengeQuestionDisplayOrder.get(triple).size() > 1) {
                    throw new InvalidImportException("DisplayOrder cannot be duplicated within case type"
                        + " and challenge question in ChallengeQuestion tab");
                }
            });
    }

    private void validateQuestionId(DefinitionDataItem definitionDataItem) {
        final String questionId = definitionDataItem.getString(ColumnName.CHALLENGE_QUESTION_QUESTION_ID);
        validateNullValue(questionId, ERROR_MESSAGE + " value: questionId cannot be null.");
    }

    private void validateAnswer(DefinitionDataItem definitionDataItem, CaseTypeEntity caseTypeEntity) {
        final String answers = definitionDataItem.getString(ColumnName.CHALLENGE_QUESTION_ANSWER_FIELD);
        validateNullValue(answers, ERROR_MESSAGE + " value: answer cannot be null.");
        final String[] answersRules = answers.split(ANSWER_MAIN_SEPARATOR);

        if (answersRules.length > 0 && answersRules.length != 1) {
            Arrays.asList(answersRules).forEach(currentAnswerExpression ->
                validateAnswerExpression(definitionDataItem, caseTypeEntity, currentAnswerExpression)
            );
        } else {
            validateAnswerExpression(definitionDataItem, caseTypeEntity, answers);
        }
    }

    private void validateAnswerExpression(DefinitionDataItem definitionDataItem,
                                          CaseTypeEntity caseTypeEntity,
                                          String currentAnswerExpression) {
        String errorMessage = ERROR_MESSAGE + " value: %s"
            + NOT_VALID + ColumnName.CHALLENGE_QUESTION_ANSWER_FIELD
            + ", Please check the expression format and the roles.";

        final String[] answersFields = currentAnswerExpression.split(Pattern.quote(ANSWER_FIELD_SEPARATOR));
        Arrays.asList(answersFields).forEach(answersField -> {
            // validate the format.
            if (!answersField.matches(ANSWER_FIELD_MATCHER)) {
                throw new InvalidImportException(String.format(errorMessage, answersField));
            }
            // validate the roles.
            if (answersField.contains(ANSWER_FIELD_ROLE_SEPARATOR)) {
                final String role = answersField.split(ANSWER_FIELD_ROLE_SEPARATOR)[1];
                final Optional<AccessProfileEntity> result = this.parseContext.getAccessProfile(
                    caseTypeEntity.getReference(),
                    role);
                if (result.isEmpty()) {
                    throw new InvalidImportException(String.format(errorMessage, answersField));
                }
            }

            // validate dot notation content.
            validateAnswerFieldExpression(definitionDataItem.getString(ColumnName.CASE_TYPE_ID), answersField);
        });
    }

    private void validateAnswerFieldExpression(String currentCaseType, String expression) {
        // remove previous validated ${} notation
        String dotNotationExpression = expression.replace("$", "")
            .replace("{", "")
            .replace("}", "");

        // remove role
        if (dotNotationExpression.contains(ANSWER_FIELD_ROLE_SEPARATOR)) {
            dotNotationExpression = dotNotationExpression.substring(0, dotNotationExpression.indexOf(":"));
        }

        try {
            // validate using common validator for dot notation
            dotNotationValidator.validate(
                this.parseContext,
                SheetName.CHALLENGE_QUESTION_TAB,
                ColumnName.CHALLENGE_QUESTION_ANSWER_FIELD,
                currentCaseType,
                dotNotationExpression);

        } catch (Exception spe) {
            throw new InvalidImportException(ERROR_MESSAGE + " value: "
                + expression + NOT_VALID + ColumnName.CHALLENGE_QUESTION_ANSWER_FIELD
                + " value. The expression dot notation values should be valid caseTypes fields.");
        }
    }

    private void validateQuestionText(DefinitionDataItem definitionDataItem) {
        final String questionText = definitionDataItem.getString(ColumnName.CHALLENGE_QUESTION_TEXT);
        validateNullValue(questionText, ERROR_MESSAGE + " value: QuestionText cannot be null.");
    }

    private void validateID(DefinitionDataItem definitionDataItem) {
        final String id = definitionDataItem.getString(ColumnName.ID);
        validateNullValue(id, ERROR_MESSAGE + " value: ID cannot be null.");
    }

    private void validateDisplayOrder(DefinitionDataItem definitionDataItem) {
        final String displayOrder = definitionDataItem.getString(ColumnName.DISPLAY_ORDER);
        try {
            Integer.parseInt(displayOrder);
        } catch (NumberFormatException numberFormatException) {
            throw new InvalidImportException(
                ERROR_MESSAGE + " value: " + displayOrder + NOT_VALID + "DisplayOrder.");
        }
    }

    private CaseTypeEntity validateCaseType(DefinitionDataItem questionItem) {
        final String caseType = questionItem.getString(ColumnName.CASE_TYPE_ID);
        Optional<CaseTypeEntity> caseTypeEntityOptional = parseContext.getCaseTypes()
            .stream()
            .filter(caseTypeEntity -> caseTypeEntity.getReference().equals(caseType))
            .findAny();
        return caseTypeEntityOptional.orElseThrow(() -> new InvalidImportException(
            ERROR_MESSAGE + " Case Type value: " + caseType + ". It cannot be found in the spreadsheet.")
        );
    }

    private FieldTypeEntity validateFieldType(DefinitionDataItem questionItem) {
        final String fieldType = questionItem.getString(ColumnName.CHALLENGE_QUESTION_ANSWER_FIELD_TYPE);
        Optional<FieldTypeEntity> fieldTypeEntity = parseContext.getType(fieldType);

        return fieldTypeEntity.orElseThrow(() -> new InvalidImportException(
            ERROR_MESSAGE + "Field Type value: " + fieldType + " cannot be found as a valid Field type."));
    }

    private void validateNullValue(String value, String message) {
        if (value == null) {
            throw new InvalidImportException(message);
        }
    }
}
