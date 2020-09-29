package uk.gov.hmcts.ccd.definition.store.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.service.question.ChallengeQuestionTabServiceImpl;
import uk.gov.hmcts.ccd.definition.store.repository.ChallengeQuestionTabRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ChallengeQuestionTabEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ChallengeQuestionTabServiceImplTest {

    @Mock
    private ChallengeQuestionTabRepository challengeQuestionTabRepository;
    private ChallengeQuestionTabServiceImpl challengeQuestionTabServiceImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        this.challengeQuestionTabServiceImpl = new ChallengeQuestionTabServiceImpl(challengeQuestionTabRepository);
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
        assertThat(result.isEmpty(), is(true));
    }
}
