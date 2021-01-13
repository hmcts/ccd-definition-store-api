package uk.gov.hmcts.ccd.definition.store.excel.parser;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.excel.validation.ChallengeQuestionValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ChallengeQuestionTabEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

@Component
public class ChallengeQuestionParser {

    private ChallengeQuestionValidator challengeQuestionValidator;

    @Autowired
    public ChallengeQuestionParser(ChallengeQuestionValidator challengeQuestionValidator) {
        this.challengeQuestionValidator = challengeQuestionValidator;
    }

    public List<ChallengeQuestionTabEntity> parse(Map<String, DefinitionSheet> definitionSheets,
                                                  ParseContext parseContext) {

        try {
            final List<DefinitionDataItem> questionItems = definitionSheets
                .get(SheetName.CHALLENGE_QUESTION_TAB.getName()).getDataItems();
            challengeQuestionValidator.validate(parseContext, questionItems);
            final List<ChallengeQuestionTabEntity> newChallengeQuestionEntities = questionItems
                .stream()
                .map(questionItem -> {
                    ChallengeQuestionTabEntity questionTabEntity =
                        createChallengeQuestionEntity(parseContext, questionItem);
                    challengeQuestionValidator.validateDisplayContext(questionTabEntity);
                    return questionTabEntity;
                }).collect(Collectors.toList());
            return newChallengeQuestionEntities;
        } catch (InvalidImportException invalidImportException) {
            ValidationResult validationResult = new ValidationResult();
            validationResult.addError(new ValidationError(invalidImportException.getMessage()) {
                @Override
                public String toString() {
                    return getDefaultMessage();
                }
            });
            throw new ValidationException(validationResult);
        }
    }

    public ChallengeQuestionTabEntity createChallengeQuestionEntity(ParseContext parseContext,
                                                                    DefinitionDataItem definitionDataItem) {
        final String questionId = definitionDataItem.getString(ColumnName.CHALLENGE_QUESTION_QUESTION_ID);
        ChallengeQuestionTabEntity challengeQuestionTabEntity = new ChallengeQuestionTabEntity();
        challengeQuestionTabEntity.setQuestionId(questionId);

        final String caseType = definitionDataItem.getString(ColumnName.CASE_TYPE_ID);
        Optional<CaseTypeEntity> caseTypeEntityOptional = parseContext.getCaseTypes()
            .stream()
            .filter(caseTypeEntity -> caseTypeEntity.getReference().equals(caseType))
            .findAny();
        if (caseTypeEntityOptional.isPresent()) {
            challengeQuestionTabEntity.setCaseType(caseTypeEntityOptional.get());
        }

        final String fieldType = definitionDataItem.getString(ColumnName.CHALLENGE_QUESTION_ANSWER_FIELD_TYPE);
        Optional<FieldTypeEntity> fieldTypeEntity = parseContext.getType(fieldType);
        if (fieldTypeEntity.isPresent()) {
            challengeQuestionTabEntity.setAnswerFieldType(fieldTypeEntity.get());
        }

        challengeQuestionTabEntity.setChallengeQuestionId(definitionDataItem.getString(ColumnName.ID));
        challengeQuestionTabEntity.setQuestionText(definitionDataItem.getString(ColumnName.CHALLENGE_QUESTION_TEXT));
        challengeQuestionTabEntity.setOrder(Integer.parseInt(definitionDataItem.getString(ColumnName.DISPLAY_ORDER)));
        challengeQuestionTabEntity.setAnswerField(definitionDataItem
            .getString(ColumnName.CHALLENGE_QUESTION_ANSWER_FIELD));
        challengeQuestionTabEntity.setDisplayContextParameter(definitionDataItem
            .getString(ColumnName.DISPLAY_CONTEXT_PARAMETER));
        return challengeQuestionTabEntity;
    }
}
