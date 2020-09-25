package uk.gov.hmcts.ccd.definition.store.excel.parser;

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
import uk.gov.hmcts.ccd.definition.store.repository.entity.ChallengeQuestionTabEntity;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ChallengeQuestionParser {

    private ChallengeQuestionValidator challengeQuestionValidator;

    @Autowired
    public ChallengeQuestionParser(ChallengeQuestionValidator challengeQuestionValidator) {
        this.challengeQuestionValidator = challengeQuestionValidator;
    }

    public List<ChallengeQuestionTabEntity> parse(Map<String, DefinitionSheet> definitionSheets, ParseContext parseContext) {

        try {
            final List<DefinitionDataItem> questionItems = definitionSheets.get(SheetName.CHALLENGE_QUESTION_TAB.getName()).getDataItems();
            validateUniqueIds(questionItems);
            challengeQuestionValidator.setDisplayOrderList(new HashMap<>());
            final List<ChallengeQuestionTabEntity> newChallengeQuestionEntities = questionItems.stream().map(questionItem -> {
                return challengeQuestionValidator.validate(parseContext, questionItem);
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

    private void validateUniqueIds(List<DefinitionDataItem> questionItems) {
        final List<String> questionsIds = questionItems.stream().map(questionItem -> {
            return questionItem.getString(ColumnName.CHALLENGE_QUESTION_QUESTION_ID);
        }).collect(Collectors.toList());

        final Set<String> duplicatedQuestionIds = findDuplicateByFrequency(questionsIds);
        if (!duplicatedQuestionIds.isEmpty()) {
            throw new InvalidImportException(ChallengeQuestionValidator.ERROR_MESSAGE + " value: "
                    + duplicatedQuestionIds.toString() + " is not a valid " + ColumnName.CHALLENGE_QUESTION_QUESTION_ID
                    + " value, QuestionId cannot be duplicated.");
        }
    }

    public <T> Set<T> findDuplicateByFrequency(List<T> list) {

        return list.stream().filter(i -> Collections.frequency(list, i) > 1)
                .collect(Collectors.toSet());

    }
}
