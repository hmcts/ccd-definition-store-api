package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ChallengeQuestionTabEntity;

import java.util.Optional;

public interface ChallengeQuestionTabRepository extends JpaRepository<ChallengeQuestionTabEntity, Integer> {

    @Query("select c from NewChallengeQuestionTabEntity c where c.questionId=:questionId ")
    Optional<ChallengeQuestionTabEntity> findByQuestionId(@Param("questionId") String questionId);
}
