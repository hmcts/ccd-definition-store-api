package uk.gov.hmcts.ccd.definition.store.domain.service.question;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.ChallengeQuestionTabRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ChallengeQuestionTabEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.ChallengeQuestion;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ChallengeQuestionTabServiceImpl implements ChallengeQuestionTabService {

    private static final Logger LOG = LoggerFactory.getLogger(ChallengeQuestionTabServiceImpl.class);
    private ChallengeQuestionTabRepository challengeQuestionTabRepository;

    private final EntityToResponseDTOMapper dtoMapper;

    @Autowired
    public ChallengeQuestionTabServiceImpl(ChallengeQuestionTabRepository challengeQuestionTabRepository,
                                           EntityToResponseDTOMapper dtoMapper) {
        this.challengeQuestionTabRepository = challengeQuestionTabRepository;
        this.dtoMapper = dtoMapper;
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

    @Override
    public List<ChallengeQuestion> getChallengeQuestions(String caseTypeId, String challengeQuestionId) {
        List<ChallengeQuestionTabEntity> questions = challengeQuestionTabRepository
            .getChallengeQuestions(caseTypeId, challengeQuestionId);
        return questions.stream()
            .map(dtoMapper::map)
            .collect(Collectors.toList());
    }
}

