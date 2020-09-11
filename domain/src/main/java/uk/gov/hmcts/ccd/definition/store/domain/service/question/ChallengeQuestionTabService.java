package uk.gov.hmcts.ccd.definition.store.domain.service.question;

import uk.gov.hmcts.ccd.definition.store.repository.entity.NewChallengeQuestionTabEntity;

import java.util.List;

public interface ChallengeQuestionTabService {

    void saveAll(List<NewChallengeQuestionTabEntity> entity);
}
