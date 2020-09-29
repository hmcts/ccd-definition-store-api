package uk.gov.hmcts.ccd.definition.store.excel.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ChallengeQuestionDisplayContextParameterValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.excel.parser.SpreadsheetParsingException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ChallengeQuestionTabEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@Component
public class ChallengeQuestionValidator {

    private ParseContext parseContext;
    private static final String ANSWER_MAIN_SEPARATOR = ",";
    private static final String ANSWER_FIELD_SEPARATOR = "|";
    private static final String ANSWER_FIELD_DOT_SEPARATOR = ".";
    private static final String ANSWER_FIELD_ROLE_SEPARATOR = ":";
    private static final String ANSWER_FIELD_MATCHER = "^\\$\\{\\S.{1,}.\\S.{1,}}$|^\\$\\{\\S.{1,}.\\S.{1,}}:\\[\\S{1,}\\]$";
    public static final String ERROR_MESSAGE = "ChallengeQuestionTab Invalid";
    private ChallengeQuestionDisplayContextParameterValidator challengeQuestionDisplayContextParameterValidator;
    private HashMap<String, List<String>> displayOrderList = new HashMap<>();

    @Autowired
    public ChallengeQuestionValidator(ChallengeQuestionDisplayContextParameterValidator challengeQuestionDisplayContextParameterValidator) {
        this.challengeQuestionDisplayContextParameterValidator = challengeQuestionDisplayContextParameterValidator;
    }

    public ChallengeQuestionTabEntity validate(ParseContext parseContext, DefinitionDataItem definitionDataItem) {

        this.parseContext = parseContext;
        validateQuestionId(definitionDataItem);
        final String questionId = definitionDataItem.getString(ColumnName.CHALLENGE_QUESTION_QUESTION_ID);
        ChallengeQuestionTabEntity challengeQuestionTabEntity = new ChallengeQuestionTabEntity();
        challengeQuestionTabEntity.setQuestionId(questionId);
        challengeQuestionTabEntity.setCaseType(getCaseTypeEntity(definitionDataItem));
        challengeQuestionTabEntity.setAnswerFieldType(getFieldTypeEntity(definitionDataItem));
        validateID(definitionDataItem, challengeQuestionTabEntity);
        validateDisplayOrder(definitionDataItem, challengeQuestionTabEntity);
        validateQuestionText(definitionDataItem, challengeQuestionTabEntity);
        validateAnswer(definitionDataItem, challengeQuestionTabEntity);
        validateDisplayContext(definitionDataItem, challengeQuestionTabEntity);
        return challengeQuestionTabEntity;
    }

    private void validateQuestionId(DefinitionDataItem definitionDataItem) {
        final String questionId = definitionDataItem.getString(ColumnName.CHALLENGE_QUESTION_QUESTION_ID);
        validateNullValue(questionId, ERROR_MESSAGE + " value: questionId cannot be null.");

    }

    private void validateAnswer(DefinitionDataItem definitionDataItem, ChallengeQuestionTabEntity challengeQuestionTabEntity) {
        final String answers = definitionDataItem.getString(ColumnName.CHALLENGE_QUESTION_ANSWER_FIELD);
        validateNullValue(answers, ERROR_MESSAGE + " value: answer cannot be null.");
        final String[] answersRules = answers.split(ANSWER_MAIN_SEPARATOR);

        if (answersRules.length > 0 && answersRules.length != 1) {
            Arrays.asList(answersRules).stream().forEach(currentAnswerExpression -> {
                        validateAnswerExpression(definitionDataItem, challengeQuestionTabEntity, currentAnswerExpression);
                    }
            );
        } else {
            validateAnswerExpression(definitionDataItem, challengeQuestionTabEntity, answers);
        }
        challengeQuestionTabEntity.setAnswerField(answers);
    }

    private void validateAnswerExpression(DefinitionDataItem definitionDataItem, ChallengeQuestionTabEntity challengeQuestionTabEntity, String currentAnswerExpression) {

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
                        final Optional<UserRoleEntity> result = this.parseContext.getRole(challengeQuestionTabEntity.getCaseType().getReference(), role);
                        if (!result.isPresent()) {
                            throw new InvalidImportException(ERROR_MESSAGE + " value: "
                                    + answersField + " is not a valid " + ColumnName.CHALLENGE_QUESTION_ANSWER_FIELD
                                    + " value. Please check the expression format and the roles.");
                        }
                    }
                    // validate dot notation content.
                    validateAnswerFieldExpression(definitionDataItem.getString(ColumnName.CASE_TYPE_ID), answersField);
                }
        );

    }

    private void validateAnswerFieldExpression(String currentCaseType, String expression) {
        final InvalidImportException invalidImportException = new InvalidImportException(ERROR_MESSAGE + " value: "
                + expression + " is not a valid " + ColumnName.CHALLENGE_QUESTION_ANSWER_FIELD
                + " value. The expression dot notation values should be valid caseTypes fields.");
        // remove previous validated ${} notation
        final String dotNotationExpression = expression.replace("$", "")
                .replace("{", "")
                .replace("}", "");


        if (!dotNotationExpression.contains(ANSWER_FIELD_DOT_SEPARATOR)) {
            validateSingleExpression(dotNotationExpression, invalidImportException, currentCaseType);
        } else {
            final String[] splittedDotNotationExpression = dotNotationExpression.split(Pattern.quote(ANSWER_FIELD_DOT_SEPARATOR));
            try {
                final FieldTypeEntity fieldType = parseContext.getCaseFieldType(currentCaseType, splittedDotNotationExpression[0]);
                final String[] attributesDotNotation = Arrays.copyOfRange(splittedDotNotationExpression, 1, splittedDotNotationExpression.length);
                IntStream.range(0, attributesDotNotation.length).forEach(index -> {
                        // Remove Role is needed.
                        if (attributesDotNotation[index].contains(ANSWER_FIELD_ROLE_SEPARATOR)) {
                            attributesDotNotation[index] = attributesDotNotation[index].substring(0, attributesDotNotation[index].indexOf(":"));
                        }
                        validateAttributes(attributesDotNotation[index], fieldType.getComplexFields(),attributesDotNotation,index);
                    }
                );
            } catch (SpreadsheetParsingException exception) {
                throw invalidImportException;
            }
        }
    }

    private void validateSingleExpression(String singleExpression, InvalidImportException invalidImportException, String currentCaseType) {

        if (singleExpression.contains(ANSWER_FIELD_ROLE_SEPARATOR)) {
            String attribute = singleExpression.substring(0, singleExpression.indexOf(":"));
            try {
                final FieldTypeEntity fieldType = parseContext.getCaseFieldType(currentCaseType, attribute);
                if (fieldType == null) {
                    throw invalidImportException;
                }
            } catch (SpreadsheetParsingException spreadsheetParsingException) {
                throw invalidImportException;
            }
        }
    }

    private void validateAttributes(String currentAttribute, List<ComplexFieldEntity> complexFieldACLEntity,
                                    String[] attributesDotNotation, int currentIndex) {
        final InvalidImportException invalidImportException = new InvalidImportException(ERROR_MESSAGE + " value: "
                + currentAttribute + " is not a valid " + ColumnName.CHALLENGE_QUESTION_ANSWER_FIELD
                + " value, The expression dot notation values should be valid caseTypes fields.");

        final Optional<ComplexFieldEntity> result = getComplexFieldEntity(complexFieldACLEntity, currentAttribute);

        if (!result.isPresent()) {
            if (currentIndex - 1 < 0) {
                throw invalidImportException;
            }
            //It means that there is a parent component.;
            final Optional<ComplexFieldEntity> parent = getComplexFieldEntity(complexFieldACLEntity, attributesDotNotation[currentIndex - 1]);
            if (parent.isPresent()) {
                final Optional<ComplexFieldEntity> attributeDefinition = getComplexFieldEntity(
                        parent.get().getFieldType().getComplexFields(),
                        currentAttribute
                );
                if (!attributeDefinition.isPresent()) {
                    throw invalidImportException;
                }
            }
        }
    }

    private Optional<ComplexFieldEntity> getComplexFieldEntity(List<ComplexFieldEntity> complexFieldACLEntity, String currentAttribute) {
        return complexFieldACLEntity.stream().filter(complexFieldACLEItem ->
                complexFieldACLEItem.getReference().equals(currentAttribute)
        ).findAny();
    }

    private void validateDisplayContext(DefinitionDataItem definitionDataItem, ChallengeQuestionTabEntity challengeQuestionTabEntity) {

        final String displayContext = definitionDataItem.getString(ColumnName.DISPLAY_CONTEXT_PARAMETER);
        challengeQuestionTabEntity.setDisplayContextParameter(displayContext);
        final ValidationResult validationResult = challengeQuestionDisplayContextParameterValidator.validate(
            challengeQuestionTabEntity,
            Collections.EMPTY_LIST
        );
        if (!validationResult.isValid()) {
            throw new ValidationException(validationResult);
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
        final String displayOrder = definitionDataItem.getString(ColumnName.DISPLAY_ORDER);
        try {
            challengeQuestionTabEntity.setOrder(Integer.parseInt(displayOrder));
            addInDisplayOrderMap(challengeQuestionTabEntity.getChallengeQuestionId(),displayOrder);
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
                new InvalidImportException(ERROR_MESSAGE + " Case Type value: " + caseType + ". It cannot be found in the spreadsheet.")
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

    private void addInDisplayOrderMap(String questionGroup, String displayOrder) {

        List<String> listOfDisplayOrder = displayOrderList.get(questionGroup);
        if (listOfDisplayOrder == null) {
            listOfDisplayOrder = new ArrayList<>();
            listOfDisplayOrder.add(displayOrder);
            displayOrderList.put(questionGroup, listOfDisplayOrder);
        } else {
            if (listOfDisplayOrder.contains(displayOrder)) {
                throw new InvalidImportException(ERROR_MESSAGE +
                        " value: " + displayOrder + ". The " + ColumnName.DISPLAY_ORDER +
                        " values must be unique in the question group " + questionGroup);
            } else {
                listOfDisplayOrder.add(displayOrder);
            }
        }
    }

    public void setDisplayOrderList(HashMap<String, List<String>> displayOrderList) {
        this.displayOrderList = displayOrderList;
    }
}
