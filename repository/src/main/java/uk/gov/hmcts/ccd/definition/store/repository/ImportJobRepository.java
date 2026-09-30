package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ImportJobEntity;

import java.util.UUID;

public interface ImportJobRepository extends JpaRepository<ImportJobEntity, UUID> {

    @Modifying
    @Query(value = """
        UPDATE import_jobs
        SET status = 'EXPIRED',
            completed_at = now()
        WHERE status IN ('PENDING', 'RUNNING')
          AND started_at < now() - make_interval(secs => :thresholdSeconds)
        """, nativeQuery = true)
    int expireStaleJobs(@Param("thresholdSeconds") int thresholdSeconds);
}
