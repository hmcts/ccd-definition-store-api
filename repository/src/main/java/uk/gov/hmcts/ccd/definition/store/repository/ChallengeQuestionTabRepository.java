package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ChallengeQuestionTabEntity;

import java.util.List;
import java.util.Optional;

public interface ChallengeQuestionTabRepository extends JpaRepository<ChallengeQuestionTabEntity, Integer> {

    @Query("select c from ChallengeQuestionTabEntity c where c.questionId=:questionId ")
    Optional<ChallengeQuestionTabEntity> findByQuestionId(@Param("questionId") String questionId);

    @Query("select c from ChallengeQuestionTabEntity c where c.caseType.reference = :caseTypeReference "
        + "and c.challengeQuestionId = :challengeQuestionId and c.caseType.version = (select max(c2.version) "
        + "from CaseTypeEntity c2 where c2.reference = c.caseType.reference)")
    List<ChallengeQuestionTabEntity> getChallengeQuestions(@Param("caseTypeReference") String caseTypeReference,
                                                           @Param("challengeQuestionId") String challengeQuestionId);
}
