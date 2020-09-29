package uk.gov.hmcts.ccd.definition.store.domain.service.question;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.ChallengeQuestionTabRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ChallengeQuestionTabEntity;

import java.util.List;
import java.util.Optional;

@Component
public class ChallengeQuestionTabServiceImpl implements ChallengeQuestionTabService {

    private static final Logger LOG = LoggerFactory.getLogger(ChallengeQuestionTabServiceImpl.class);
    private ChallengeQuestionTabRepository challengeQuestionTabRepository;

    @Autowired
    public ChallengeQuestionTabServiceImpl(ChallengeQuestionTabRepository challengeQuestionTabRepository) {
        this.challengeQuestionTabRepository = challengeQuestionTabRepository;
    }

    @Override
    public void saveAll(List<ChallengeQuestionTabEntity> entity) {
        LOG.debug("Create ChallengeQuestionTabEntity Entity {}", entity);
        this.challengeQuestionTabRepository.saveAll(entity);
    }

    @Override
    public Optional<ChallengeQuestionTabEntity> findByQuestionId(String questionId) {
        return challengeQuestionTabRepository.findByQuestionId(questionId);
    }
}

