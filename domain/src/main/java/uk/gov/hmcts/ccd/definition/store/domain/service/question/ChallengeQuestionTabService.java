package uk.gov.hmcts.ccd.definition.store.domain.service.question;

import uk.gov.hmcts.ccd.definition.store.repository.entity.ChallengeQuestionTabEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.ChallengeQuestion;

import java.util.List;
import java.util.Optional;

public interface ChallengeQuestionTabService {

    void saveAll(List<ChallengeQuestionTabEntity> entity);

    Optional<ChallengeQuestionTabEntity> findByQuestionId(String questionId);

    List<ChallengeQuestion> getChallengeQuestions(String caseTypeReference, String challengeQuestionId);
}
