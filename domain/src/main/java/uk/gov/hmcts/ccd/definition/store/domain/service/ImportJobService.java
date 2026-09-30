package uk.gov.hmcts.ccd.definition.store.domain.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.domain.ApplicationParams;
import uk.gov.hmcts.ccd.definition.store.repository.ImportJobRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ImportJobEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ImportJobStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class ImportJobService {
    private final ImportJobRepository repository;
    private final ApplicationParams applicationParams;

    @PersistenceContext
    private EntityManager entityManager;

    private static final int MAX_ERROR_SUMMARY_LENGTH = 2000;

    public ImportJobService(ImportJobRepository repository, ApplicationParams applicationParams) {
        this.repository = repository;
        this.applicationParams = applicationParams;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UUID createPending(UUID providedId, String submittedBy) {
        UUID id = (providedId != null) ? providedId : UUID.randomUUID();

        ImportJobEntity entity = new ImportJobEntity();
        entity.setId(id);
        entity.setStatus(ImportJobStatus.PENDING);
        entity.setSubmittedBy(submittedBy);
        LocalDateTime now = LocalDateTime.now();
        entity.setSubmittedAt(now);
        entity.setStartedAt(now);
        try {
            entityManager.persist(entity);
            entityManager.flush();
        } catch (PersistenceException ex) {
            throw new DataIntegrityViolationException(
                "An import job with id " + id + " already exists", ex);
        }

        log.info("Created import job {} for submitter {}", id, submittedBy);

        return id;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCompleted(UUID id, List<String> warningsSnapshot, String reindexTaskId) {
        repository.findById(id)
            .ifPresentOrElse(entity -> {
                // Intentional: if sweep already set EXPIRED (long-running import),
                // COMPLETED is still the correct terminal state.
                entity.setStatus(ImportJobStatus.COMPLETED);
                entity.setCompletedAt(LocalDateTime.now());
                entity.setWarnings(serialiseWarnings(warningsSnapshot));
                entity.setReindexTaskId(reindexTaskId);

                repository.save(entity);

                log.info("Marked import job {} as COMPLETED", id);
            }, () -> log.warn("markCompleted called for unknown import job id {}", id));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(UUID id, String errorSummary) {
        repository.findById(id)
            .ifPresentOrElse(entity -> {
                entity.setStatus(ImportJobStatus.FAILED);
                entity.setCompletedAt(LocalDateTime.now());

                String truncatedSummary = (errorSummary != null && errorSummary.length() > MAX_ERROR_SUMMARY_LENGTH)
                    ? errorSummary.substring(0, MAX_ERROR_SUMMARY_LENGTH)
                    : errorSummary;

                entity.setErrorSummary(truncatedSummary);
                repository.save(entity);

                log.info("Marked import job {} as FAILED", id);
            }, () -> log.warn("markFailed called for unknown import job id {}", id));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int expireStaleJobs() {
        int thresholdSeconds = applicationParams.getImportJobStaleThresholdSeconds();
        int count = repository.expireStaleJobs(thresholdSeconds);
        if (count > 0) {
            log.info("Expired {} stale import job(s)", count);
        }
        return count;
    }

    @Transactional(readOnly = true)
    public Optional<ImportJobEntity> findById(UUID id) {
        return repository.findById(id);
    }

    private String serialiseWarnings(List<String> warnings) {
        if (warnings == null || warnings.isEmpty()) {
            return null;
        }
        return String.join("\n", warnings);
    }
}
