package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.excel.validation.ChallengeQuestionValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ChallengeQuestionTabEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


public class ChallengeQuestionParserTest {

    @Mock
    private ChallengeQuestionValidator challengeQuestionValidator;
    private ChallengeQuestionParser challengeQuestionParser;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(challengeQuestionValidator.validate(any(), any())).thenReturn(createChallengeQuestionTabEntity());
        this.challengeQuestionParser = new ChallengeQuestionParser(challengeQuestionValidator);
    }

    @Test
    public void testParse() {
        final List<ChallengeQuestionTabEntity> challengeQuestionTabEntities =
                challengeQuestionParser.parse(createDefinitionSheets(false), new ParseContext());
        assertThat(challengeQuestionTabEntities.size(), is(1));
        assertThat(challengeQuestionTabEntities.get(0).getQuestionId(), is("questionID1"));
    }

    @Test(expected = ValidationException.class)
    public void failDueToDuplicatedIDs() {
        challengeQuestionParser.parse(createDefinitionSheets(true), new ParseContext());
    }

    private Map<String, DefinitionSheet> createDefinitionSheets(boolean failTest) {
        final Map<String, DefinitionSheet> map = new HashMap<>();
        DefinitionSheet definitionSheet1 = new DefinitionSheet();
        definitionSheet1.setName(SheetName.CHALLENGE_QUESTION_TAB.getName());

        DefinitionDataItem definitionDataItem1 = new DefinitionDataItem(SheetName.CHALLENGE_QUESTION_TAB.getName());
        definitionDataItem1.addAttribute(ColumnName.CHALLENGE_QUESTION_QUESTION_ID, "questionID1");
        definitionSheet1.addDataItem(definitionDataItem1);

        if (failTest) {
            DefinitionDataItem definitionDataItem2 = new DefinitionDataItem(SheetName.CHALLENGE_QUESTION_TAB.getName());
            definitionDataItem2.addAttribute(ColumnName.CHALLENGE_QUESTION_QUESTION_ID, "questionID1");
            definitionSheet1.addDataItem(definitionDataItem2);
        }
        map.put(SheetName.CHALLENGE_QUESTION_TAB.getName(), definitionSheet1);
        return map;
    }

    private ChallengeQuestionTabEntity createChallengeQuestionTabEntity() {
        final ChallengeQuestionTabEntity challengeQuestionTabEntity = new ChallengeQuestionTabEntity();
        challengeQuestionTabEntity.setQuestionId("questionID1");
        return challengeQuestionTabEntity;
    }
}
