package uk.gov.hmcts.ccd.definition.store.elastic.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.ReindexRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ReindexEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.ReindexDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletionException;

@Service
@Slf4j
public class ReindexTaskServiceImpl implements ReindexTaskService {

    private final ReindexRepository reindexRepository;
    private final EntityToResponseDTOMapper mapper;

    @Autowired
    public ReindexTaskServiceImpl(ReindexRepository reindexRepository, EntityToResponseDTOMapper mapper) {
        this.reindexRepository = reindexRepository;
        this.mapper = mapper;
    }

    @Override
    public List<ReindexDTO> getAll() {
        return reindexRepository.findAll()
            .stream()
            .map(mapper::map)
            .toList();
    }

    @Override
    public List<ReindexDTO> getTasksByCaseType(String caseType) {
        if (StringUtils.isBlank(caseType)) {
            return getAll();
        }
        return reindexRepository.findByCaseType(caseType)
            .stream()
            .map(mapper::map)
            .toList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReindexEntity saveEntity(Boolean reindex, Boolean deleteOldIndex, CaseTypeEntity caseType,
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateEntity(String newIndexName, String response) {
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateEntity(String newIndexName, Exception ex) {
        ReindexEntity reindexEntity = reindexRepository.findByIndexName(newIndexName).orElse(null);
        if (reindexEntity == null) {
            String message = String.format("No reindex entity metadata found for index name: %s", newIndexName);
            log.error(message);
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
