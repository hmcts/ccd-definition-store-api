package uk.gov.hmcts.ccd.definition.store.excel.parser;

import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.excel.challengequestion.BaseChallengeQuestionTest;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.excel.validation.ChallengeQuestionValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ChallengeQuestionTabEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;


class ChallengeQuestionParserTest extends BaseChallengeQuestionTest {

    @Mock
    private ChallengeQuestionValidator challengeQuestionValidator;
    private ChallengeQuestionParser challengeQuestionParser;
    private ParseContext parseContext;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        parseContext = buildParseContext();
        this.challengeQuestionParser = new ChallengeQuestionParser(challengeQuestionValidator);
    }

    @Test
    void testParse() {
        final List<ChallengeQuestionTabEntity> challengeQuestionTabEntities =
                challengeQuestionParser.parse(createDefinitionSheets(false), parseContext);
        assertThat(challengeQuestionTabEntities.size(), is(1));
        assertThat(challengeQuestionTabEntities.get(0).getQuestionId(), is("questionID1"));
    }

    @Test
    void testIgnoreNullFields_True() {
        final List<ChallengeQuestionTabEntity> challengeQuestionTabEntities =
            getChallengeQuestionTabEntities("true");
        assertThat(challengeQuestionTabEntities.size(), is(1));
        assertThat(challengeQuestionTabEntities.get(0).getAnswerField(), is("${OrganisationField}:[CLAIMANT]"));
        assertThat(challengeQuestionTabEntities.get(0).isIgnoreNullFields(), is(true));
    }

    @Test
    void testIgnoreNullFields_Yes() {
        final List<ChallengeQuestionTabEntity> challengeQuestionTabEntities =
            getChallengeQuestionTabEntities("Yes");
        assertThat(challengeQuestionTabEntities.size(), is(1));
        assertThat(challengeQuestionTabEntities.get(0).getAnswerField(), is("${OrganisationField}:[CLAIMANT]"));
        assertThat(challengeQuestionTabEntities.get(0).isIgnoreNullFields(), is(true));
    }

    @Test
    void testIgnoreNullFields_Y() {
        final List<ChallengeQuestionTabEntity> challengeQuestionTabEntities =
            getChallengeQuestionTabEntities("Y");
        assertThat(challengeQuestionTabEntities.size(), is(1));
        assertThat(challengeQuestionTabEntities.get(0).getAnswerField(), is("${OrganisationField}:[CLAIMANT]"));
        assertThat(challengeQuestionTabEntities.get(0).isIgnoreNullFields(), is(true));
    }

    @Test
    void testIgnoreNullFields_False() {
        final List<ChallengeQuestionTabEntity> challengeQuestionTabEntities =
            getChallengeQuestionTabEntities("false");
        assertThat(challengeQuestionTabEntities.size(), is(1));
        assertThat(challengeQuestionTabEntities.get(0).getAnswerField(), is("${OrganisationField}:[CLAIMANT]"));
        assertThat(challengeQuestionTabEntities.get(0).isIgnoreNullFields(), is(false));
    }

    @Test
    void testIgnoreNullFields_No() {
        final List<ChallengeQuestionTabEntity> challengeQuestionTabEntities =
            getChallengeQuestionTabEntities("No");
        assertThat(challengeQuestionTabEntities.size(), is(1));
        assertThat(challengeQuestionTabEntities.get(0).getAnswerField(), is("${OrganisationField}:[CLAIMANT]"));
        assertThat(challengeQuestionTabEntities.get(0).isIgnoreNullFields(), is(false));
    }

    @Test
    void testIgnoreNullFields_N() {
        final List<ChallengeQuestionTabEntity> challengeQuestionTabEntities =
            getChallengeQuestionTabEntities("N");
        assertThat(challengeQuestionTabEntities.size(), is(1));
        assertThat(challengeQuestionTabEntities.get(0).getAnswerField(), is("${OrganisationField}:[CLAIMANT]"));
        assertThat(challengeQuestionTabEntities.get(0).isIgnoreNullFields(), is(false));
    }

    @Test
    void testIgnoreNullFields_Default() {
        final List<ChallengeQuestionTabEntity> challengeQuestionTabEntities =
            getChallengeQuestionTabEntities(null);
        assertThat(challengeQuestionTabEntities.size(), is(1));
        assertThat(challengeQuestionTabEntities.get(0).getAnswerField(), is("${OrganisationField}:[CLAIMANT]"));
        assertThat(challengeQuestionTabEntities.get(0).isIgnoreNullFields(), is(false));
    }

    private List<ChallengeQuestionTabEntity> getChallengeQuestionTabEntities(String ignoreField) {
        Map<String, DefinitionSheet> map = createDefinitionSheets(false);
        DefinitionSheet definitionSheet1 = map.get(SheetName.CHALLENGE_QUESTION_TAB.getName());
        DefinitionDataItem definitionDataItem = definitionSheet1.getDataItems().get(0);
        definitionDataItem.addAttribute(ColumnName.CHALLENGE_QUESTION_ANSWER_FIELD.toString(),
            "${OrganisationField}:[CLAIMANT]");
        definitionDataItem.addAttribute(ColumnName.CHALLENGE_QUESTION_IGNORE_NULL_FIELDS.toString(), ignoreField);
        return challengeQuestionParser.parse(map, parseContext);
    }

    @Test
    void failDueToDuplicatedIDs() {
        doThrow(new ValidationException(new ValidationResult()))
            .when(challengeQuestionValidator)
            .validate(any(), any());
        assertThrows(ValidationException.class, () ->
            challengeQuestionParser.parse(createDefinitionSheets(true), parseContext));
    }

    private Map<String, DefinitionSheet> createDefinitionSheets(boolean failTest) {
        final Map<String, DefinitionSheet> map = new HashMap<>();
        DefinitionSheet definitionSheet1 = new DefinitionSheet();
        definitionSheet1.setName(SheetName.CHALLENGE_QUESTION_TAB.getName());

        DefinitionDataItem definitionDataItem1 = new DefinitionDataItem(SheetName.CHALLENGE_QUESTION_TAB.getName());
        definitionDataItem1.addAttribute(ColumnName.CHALLENGE_QUESTION_QUESTION_ID, "questionID1");
        definitionDataItem1.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE);
        definitionDataItem1.addAttribute(ColumnName.CHALLENGE_QUESTION_ANSWER_FIELD_TYPE, FIELD_TYPE);
        definitionDataItem1.addAttribute(ColumnName.DISPLAY_ORDER, 1);

        definitionSheet1.addDataItem(definitionDataItem1);

        if (failTest) {
            DefinitionDataItem definitionDataItem2 = new DefinitionDataItem(SheetName.CHALLENGE_QUESTION_TAB.getName());
            definitionDataItem2.addAttribute(ColumnName.CHALLENGE_QUESTION_QUESTION_ID, "questionID1");
            definitionDataItem2.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE);
            definitionDataItem2.addAttribute(ColumnName.CHALLENGE_QUESTION_ANSWER_FIELD_TYPE, FIELD_TYPE);
            definitionDataItem2.addAttribute(ColumnName.DISPLAY_ORDER, 2);
            definitionSheet1.addDataItem(definitionDataItem2);
        }
        map.put(SheetName.CHALLENGE_QUESTION_TAB.getName(), definitionSheet1);
        return map;
    }
}
