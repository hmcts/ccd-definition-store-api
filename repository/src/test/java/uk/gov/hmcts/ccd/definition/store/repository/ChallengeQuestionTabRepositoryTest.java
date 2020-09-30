package uk.gov.hmcts.ccd.definition.store.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ChallengeQuestionTabEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@RunWith(SpringRunner.class)
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

    private CaseTypeEntity oldCaseType;
    private CaseTypeEntity latestCaseType;
    private CaseTypeEntity otherCaseType;
    private FieldTypeEntity fieldType;

    @Before
    public void setUp() {
        oldCaseType = testHelper.createCaseType(CASE_TYPE_REFERENCE, CASE_TYPE_REFERENCE);
        latestCaseType = testHelper.createCaseType(CASE_TYPE_REFERENCE, CASE_TYPE_REFERENCE);
        otherCaseType = testHelper.createCaseType("OtherCaseType", "Other");
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

    private ChallengeQuestionTabEntity createChallengeQuestion(CaseTypeEntity caseType,
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
        return challengeQuestionTabRepository.save(challengeQuestion);
    }
}
