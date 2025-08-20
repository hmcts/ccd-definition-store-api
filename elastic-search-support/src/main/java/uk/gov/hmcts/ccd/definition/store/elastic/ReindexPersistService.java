package uk.gov.hmcts.ccd.definition.store.elastic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

    @Transactional
    public static ReindexEntity initiateReindex(String caseTypeName, boolean reindex, boolean deleteOldIndex, CaseTypeEntity caseType,
                                                String newIndexName, ReindexRepository reindexRepository) {
        ReindexEntity entity = reindexRepository.findByIndexName(caseTypeName).orElse(null);
        if (entity == null) {
            log.info("No existing reindex metadata found for case type: {}, inserting to DB", caseTypeName);
            entity = new ReindexEntity();
        }
        entity.setIndexName(caseTypeName);
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
        ReindexEntity entity = reindexRepository.findByIndexName(caseTypeName).orElse(null);
        if (entity == null) {
            log.warn("No reindex metadata found for case type: {}", caseTypeName);
            return;
        }
        log.info("Persistence completed successfully for case type: {}", caseTypeName);
        entity.setStatus("SUCCESS");
        entity.setEndTime(LocalDateTime.now());
        reindexRepository.save(entity);
    }

    @Transactional
    public void markFailure(String caseTypeName, Exception ex) {
        ReindexEntity entity = reindexRepository.findByIndexName(caseTypeName).orElse(null);
        if (entity == null) {
            log.warn("No reindex metadata found for case type: {}", caseTypeName);
            return;
        }
        entity.setStatus("FAILED");
        entity.setEndTime(LocalDateTime.now());
        Throwable rootCause = unwrapCompletionException(ex);
        entity.setMessage(rootCause.getClass().getName() + ": " + rootCause.getMessage());
        entity.setMessage(ex.getMessage());
        reindexRepository.save(entity);
    }

    private Throwable unwrapCompletionException(Throwable exc) {
        if (exc instanceof CompletionException && exc.getCause() != null) {
            return exc.getCause();
        }
        return exc;
    }
}