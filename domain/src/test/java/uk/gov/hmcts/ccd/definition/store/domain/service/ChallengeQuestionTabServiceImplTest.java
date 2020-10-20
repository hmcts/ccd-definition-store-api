package uk.gov.hmcts.ccd.definition.store.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.service.question.ChallengeQuestionTabServiceImpl;
import uk.gov.hmcts.ccd.definition.store.repository.ChallengeQuestionTabRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ChallengeQuestionTabEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.ChallengeQuestion;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


public class ChallengeQuestionTabServiceImplTest {

    @Mock
    private ChallengeQuestionTabRepository challengeQuestionTabRepository;
    @Mock
    private EntityToResponseDTOMapper dtoMapper;
    private ChallengeQuestionTabServiceImpl challengeQuestionTabServiceImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        this.challengeQuestionTabServiceImpl = new ChallengeQuestionTabServiceImpl(
            challengeQuestionTabRepository, dtoMapper);
    }

    @DisplayName("should save all NewChallengeQuestionTabEntity")
    @Test
    void testSaveAll() {
        List<ChallengeQuestionTabEntity> entities = Arrays.asList(
            new ChallengeQuestionTabEntity(), new ChallengeQuestionTabEntity());
        when(challengeQuestionTabRepository.saveAll(entities)).thenReturn(entities);
        challengeQuestionTabServiceImpl.saveAll(entities);
        verify(challengeQuestionTabRepository).saveAll(entities);
    }

    @DisplayName("should findByQuestionId  return a NewChallengeQuestionTabEntity")
    @Test
    void testFindByQuestionId() {
        final String questionId = "questionId";
        ChallengeQuestionTabEntity entity = new ChallengeQuestionTabEntity();
        entity.setQuestionId("questionId");
        Optional<ChallengeQuestionTabEntity> mockResult = Optional.of(entity);
        when(challengeQuestionTabRepository.findByQuestionId(questionId)).thenReturn(mockResult);
        Optional<ChallengeQuestionTabEntity> result = challengeQuestionTabServiceImpl.findByQuestionId(questionId);
        verify(challengeQuestionTabRepository).findByQuestionId(questionId);
        assertThat(result.get().getQuestionId(), is(questionId));
    }

    @DisplayName("should fail findByQuestionId due to null question id.")
    @Test
    void failFindByQuestionIdDueToANullQuestionId() {
        final String questionId = null;
        Optional<ChallengeQuestionTabEntity> mockResult = Optional.ofNullable(null);
        when(challengeQuestionTabRepository.findByQuestionId(questionId)).thenReturn(mockResult);
        Optional<ChallengeQuestionTabEntity> result = challengeQuestionTabServiceImpl.findByQuestionId(questionId);
        verify(challengeQuestionTabRepository).findByQuestionId(questionId);
        assertThat(result.isPresent(), is(false));
    }

    @Test
    void shouldGetChallengeQuestions() {
        ChallengeQuestionTabEntity entity1 = new ChallengeQuestionTabEntity();
        ChallengeQuestionTabEntity entity2 = new ChallengeQuestionTabEntity();
        ChallengeQuestion dto1 = new ChallengeQuestion();
        ChallengeQuestion dto2 = new ChallengeQuestion();
        when(challengeQuestionTabRepository.getChallengeQuestions("CaseTypeId", "ChallengeQuestionId"))
            .thenReturn(Arrays.asList(entity1, entity2));
        when(dtoMapper.map(entity1)).thenReturn(dto1);
        when(dtoMapper.map(entity2)).thenReturn(dto2);

        List<ChallengeQuestion> result = challengeQuestionTabServiceImpl.getChallengeQuestions(
            "CaseTypeId", "ChallengeQuestionId");

        assertAll(
            () -> assertThat(result.size(), is(2)),
            () -> assertThat(result.get(0), is(dto1)),
            () -> assertThat(result.get(1), is(dto2))
        );
    }

    @Test
    void shouldReturnEmptyListOfChallengeQuestionsWhenInvalidArgumentsPassed() {
        when(challengeQuestionTabRepository.getChallengeQuestions("CaseTypeId", "ChallengeQuestionId"))
            .thenReturn(singletonList(new ChallengeQuestionTabEntity()));

        List<ChallengeQuestion> result = challengeQuestionTabServiceImpl.getChallengeQuestions("UNKNOWN", "UNKNOWN");

        assertAll(
            () -> assertThat(result.isEmpty(), is(true)),
            () -> verify(challengeQuestionTabRepository).getChallengeQuestions("UNKNOWN", "UNKNOWN"),
            () -> verifyNoMoreInteractions(dtoMapper)
        );
    }
}
