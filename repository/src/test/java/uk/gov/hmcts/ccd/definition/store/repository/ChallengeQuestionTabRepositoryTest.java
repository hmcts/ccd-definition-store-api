package uk.gov.hmcts.ccd.definition.store.repository;

import com.vladmihalcea.sql.SQLStatementCountValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ChallengeQuestionTabEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
public class ChallengeQuestionTabRepositoryTest {

    private static final String CASE_TYPE_REFERENCE = "CaseTypeA";
    private static final String CHALLENGE_QUESTION_ID = "ChallengeQuestionId1";

    @Autowired
    private ChallengeQuestionTabRepository challengeQuestionTabRepository;

    @Autowired
    private TestHelper testHelper;

    private CaseTypeLiteEntity oldCaseType;
    private CaseTypeLiteEntity latestCaseType;
    private CaseTypeLiteEntity otherCaseType;
    private FieldTypeEntity fieldType;

    @Before
    public void setUp() {
        oldCaseType = testHelper.createCaseTypeLiteEntity(CASE_TYPE_REFERENCE, CASE_TYPE_REFERENCE);
        latestCaseType = testHelper.createCaseTypeLiteEntity(CASE_TYPE_REFERENCE, CASE_TYPE_REFERENCE);
        otherCaseType = testHelper.createCaseTypeLiteEntity("OtherCaseType", "Other");
        fieldType = testHelper.createType(testHelper.createJurisdiction());

        createChallengeQuestion(oldCaseType, "QuestionId1", CHALLENGE_QUESTION_ID, "1");
        createChallengeQuestion(latestCaseType, "QuestionId2", CHALLENGE_QUESTION_ID, "2");
        createChallengeQuestion(latestCaseType, "QuestionId3", CHALLENGE_QUESTION_ID, "3");
        createChallengeQuestion(latestCaseType, "QuestionId4", "ChallengeQuestionId2", "4");
        createChallengeQuestion(otherCaseType, "QuestionId5", CHALLENGE_QUESTION_ID, "5");
    }

    @Test
    public void shouldGetChallengeQuestions() {
        List<ChallengeQuestionTabEntity> result = challengeQuestionTabRepository
            .getChallengeQuestions(CASE_TYPE_REFERENCE, CHALLENGE_QUESTION_ID);

        assertAll(
            () -> assertThat(result, hasSize(2)),
            () -> assertThat(result.get(0).getCaseType(), is(latestCaseType)),
            () -> assertThat(result.get(1).getCaseType(), is(latestCaseType)),
            () -> assertThat(result.get(0).isIgnoreNullFields(), is(false)),
            () -> assertThat(result.get(1).isIgnoreNullFields(), is(false)),
            () -> assertThat(result, hasItem(hasProperty("challengeQuestionId", is(CHALLENGE_QUESTION_ID)))),
            () -> assertThat(result, hasItem(hasProperty("challengeQuestionId", is(CHALLENGE_QUESTION_ID)))),
            () -> assertThat(result, hasItem(hasProperty("questionId", is("QuestionId2")))),
            () -> assertThat(result, hasItem(hasProperty("questionId", is("QuestionId3")))),
            () -> assertThat(result, hasItem(hasProperty("answerField", is("AnswerField2")))),
            () -> assertThat(result, hasItem(hasProperty("answerField", is("AnswerField3"))))
        );
    }

    @Test
    public void shouldReturnNoChallengeQuestionsForUnknownParameters() {
        List<ChallengeQuestionTabEntity> result = challengeQuestionTabRepository
            .getChallengeQuestions("Unknown", "Unknown");

        assertAll(
            () -> assertThat(result, hasSize(0))
        );
    }

    @Test
    public void checkSQLStatementCounts() {

        SQLStatementCountValidator.reset();

        List<ChallengeQuestionTabEntity> result = challengeQuestionTabRepository
            .getChallengeQuestions(CASE_TYPE_REFERENCE, CHALLENGE_QUESTION_ID);

        assertThat(result.size(), is(2));
        assertThat(result.get(0).getCaseType().getReference(), is(CASE_TYPE_REFERENCE));

        SQLStatementCountValidator.assertSelectCount(1);
    }

    private ChallengeQuestionTabEntity createChallengeQuestion(CaseTypeLiteEntity caseType,
                                                               String questionId,
                                                               String challengeQuestionId,
                                                               String suffix) {
        ChallengeQuestionTabEntity challengeQuestion = new ChallengeQuestionTabEntity();
        challengeQuestion.setQuestionText("QuestionText" + suffix);
        challengeQuestion.setQuestionId(questionId);
        challengeQuestion.setChallengeQuestionId(challengeQuestionId);
        challengeQuestion.setDisplayContextParameter("DCP" + suffix);
        challengeQuestion.setAnswerFieldType(fieldType);
        challengeQuestion.setAnswerField("AnswerField" + suffix);
        challengeQuestion.setCaseType(caseType);
        challengeQuestion.setOrder(1);
        challengeQuestion.setIgnoreNullFields(false);
        return challengeQuestionTabRepository.save(challengeQuestion);
    }
}
