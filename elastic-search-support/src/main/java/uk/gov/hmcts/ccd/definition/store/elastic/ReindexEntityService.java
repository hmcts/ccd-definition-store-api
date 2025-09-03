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
public class ReindexEntityService {
    private final ReindexRepository reindexRepository;

    public ReindexEntityService(ReindexRepository reindexRepository) {
        this.reindexRepository = reindexRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReindexEntity persistInitialReindexMetadata(Boolean reindex, Boolean deleteOldIndex, CaseTypeEntity caseType,
                                                       String newIndexName) {
        ReindexEntity entity = new ReindexEntity();
        entity.setReindex(reindex);
        entity.setDeleteOldIndex(deleteOldIndex);
        entity.setCaseType(caseType.getReference());
        entity.setJurisdiction(caseType.getJurisdiction().getReference());
        entity.setIndexName(newIndexName);
        entity.setStartTime(LocalDateTime.now());
        entity.setStatus("STARTED");
        return reindexRepository.saveAndFlush(entity);
    }

    @Transactional
    public void persistSuccess(String newIndexName, String response) {
        ReindexEntity reindexEntity = reindexRepository.findByIndexName(newIndexName).orElse(null);
        if (reindexEntity == null) {
            String message = String.format("No reindex entity metadata found for index name: %s", newIndexName);
            log.error(message);
            throw new IllegalStateException(message);
        }
        log.info("Save to DB successful for case type: {}", newIndexName);
        reindexEntity.setStatus("SUCCESS");
        reindexEntity.setEndTime(LocalDateTime.now());
        reindexEntity.setReindexResponse(response);
        reindexRepository.save(reindexEntity);
    }

    @Transactional
    public void persistFailure(String newIndexName, Exception ex) {
        ReindexEntity reindexEntity = reindexRepository.findByIndexName(newIndexName).orElse(null);
        if (reindexEntity == null) {
            log.warn("No reindex entity metadata found for case type: {}", newIndexName);
            return;
        }
        log.info("Persisting FAILED status for index '{}'", newIndexName);
        reindexEntity.setStatus("FAILED");
        reindexEntity.setEndTime(LocalDateTime.now());
        Throwable rootCause = unwrapCompletionException(ex);
        reindexEntity.setExceptionMessage(rootCause.getClass().getName() + ": " + rootCause.getMessage());
        reindexRepository.save(reindexEntity);
    }

    private Throwable unwrapCompletionException(Throwable exc) {
        if (exc instanceof CompletionException && exc.getCause() != null) {
            return exc.getCause();
        }
        return exc;
    }
}