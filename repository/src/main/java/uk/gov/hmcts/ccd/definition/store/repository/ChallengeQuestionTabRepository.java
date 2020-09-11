package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.NewChallengeQuestionTabEntity;

public interface ChallengeQuestionTabRepository extends JpaRepository<NewChallengeQuestionTabEntity, Integer> {

}
