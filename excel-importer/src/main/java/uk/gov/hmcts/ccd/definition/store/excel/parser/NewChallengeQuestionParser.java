package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.excel.validation.NewChallengeQuestionValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.NewChallengeQuestionTabEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class NewChallengeQuestionParser {

    private NewChallengeQuestionValidator newChallengeQuestionValidator;

    @Autowired
    public NewChallengeQuestionParser(NewChallengeQuestionValidator newChallengeQuestionValidator) {
        this.newChallengeQuestionValidator = newChallengeQuestionValidator;
    }

    public List<NewChallengeQuestionTabEntity> parse(Map<String, DefinitionSheet> definitionSheets, ParseContext parseContext) {

        final List<DefinitionDataItem> questionItems = definitionSheets.get(SheetName.CHALLENGE_QUESTION_TAB.getName()).getDataItems();

        final List<NewChallengeQuestionTabEntity> newChallengeQuestionEntities = questionItems.stream().map(questionItem -> {
            return newChallengeQuestionValidator.validate(parseContext, questionItem);
        }).collect(Collectors.toList());

        return newChallengeQuestionEntities;
    }
}
