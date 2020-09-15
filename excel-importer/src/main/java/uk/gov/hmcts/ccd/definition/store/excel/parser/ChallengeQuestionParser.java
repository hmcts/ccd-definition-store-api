package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.excel.validation.ChallengeQuestionValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ChallengeQuestionTabEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ChallengeQuestionParser {

    private ChallengeQuestionValidator challengeQuestionValidator;

    @Autowired
    public ChallengeQuestionParser(ChallengeQuestionValidator challengeQuestionValidator) {
        this.challengeQuestionValidator = challengeQuestionValidator;
    }

    public List<ChallengeQuestionTabEntity> parse(Map<String, DefinitionSheet> definitionSheets, ParseContext parseContext) {

        final List<DefinitionDataItem> questionItems = definitionSheets.get(SheetName.CHALLENGE_QUESTION_TAB.getName()).getDataItems();

        final List<ChallengeQuestionTabEntity> newChallengeQuestionEntities = questionItems.stream().map(questionItem -> {
            return challengeQuestionValidator.validate(parseContext, questionItem);
        }).collect(Collectors.toList());

        return newChallengeQuestionEntities;
    }
}
