package uk.gov.hmcts.ccd.definition.store.elastic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.ReindexRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ReindexEntity;

import java.time.LocalDateTime;
import java.util.concurrent.CompletionException;

@Slf4j
@Service
public class ReindexPersistService {
    private final ReindexRepository reindexRepository;

    public ReindexPersistService(ReindexRepository reindexRepository) {
        this.reindexRepository = reindexRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReindexEntity initiateReindex(Boolean reindex, Boolean deleteOldIndex, CaseTypeEntity caseType,
                                                ReindexRepository reindexRepository, String oldIndexName,
                                                String newIndexName) {
        ReindexEntity entity = reindexRepository.findByIndexName(oldIndexName).orElse(null);
        if (entity == null) {
            log.info("No existing reindex metadata found for case type: {}, inserting to DB", oldIndexName);
            entity = new ReindexEntity();
        }
        entity.setReindex(reindex);
        entity.setDeleteOldIndex(deleteOldIndex);
        entity.setCaseType(caseType.getReference());
        entity.setJurisdiction(caseType.getJurisdiction().getReference());
        entity.setStartTime(LocalDateTime.now());
        entity.setStatus("STARTED");
        entity.setIndexName(newIndexName);
        return reindexRepository.saveAndFlush(entity);
    }

    @Transactional
    public void markSuccess(String caseTypeName) {
        ReindexEntity reindexEntity = reindexRepository.findByIndexName(caseTypeName).orElse(null);
        if (reindexEntity == null) {
            log.warn("No reindex metadata found for case type: {}", caseTypeName);
            return;
        }
        log.info("Save to DB successful for case type: {}", caseTypeName);
        reindexEntity.setStatus("SUCCESS");
        reindexEntity.setEndTime(LocalDateTime.now());
        reindexRepository.save(reindexEntity);
    }

    @Transactional
    public void markFailure(String caseTypeName, Exception ex) {
        ReindexEntity reindexEntity = reindexRepository.findByIndexName(caseTypeName).orElse(null);
        if (reindexEntity == null) {
            log.warn("No reindex metadata found for case type: {}", caseTypeName);
            return;
        }
        log.info("Save to DB failed for case type: {}", caseTypeName);
        reindexEntity.setStatus("FAILED");
        reindexEntity.setEndTime(LocalDateTime.now());
        Throwable rootCause = unwrapCompletionException(ex);
        reindexEntity.setMessage(rootCause.getClass().getName() + ": " + rootCause.getMessage());
        reindexEntity.setMessage(ex.getMessage());
        reindexRepository.save(reindexEntity);
    }

    private Throwable unwrapCompletionException(Throwable exc) {
        if (exc instanceof CompletionException && exc.getCause() != null) {
            return exc.getCause();
        }
        return exc;
    }
}