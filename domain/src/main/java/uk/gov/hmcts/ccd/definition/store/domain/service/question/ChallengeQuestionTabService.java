package uk.gov.hmcts.ccd.definition.store.domain.service.question;

import uk.gov.hmcts.ccd.definition.store.repository.entity.ChallengeQuestionTabEntity;

import java.util.List;
import java.util.Optional;

public interface ChallengeQuestionTabService {

    void saveAll(List<ChallengeQuestionTabEntity> entity);

    Optional<ChallengeQuestionTabEntity> findByQuestionId(String questionId);
}
