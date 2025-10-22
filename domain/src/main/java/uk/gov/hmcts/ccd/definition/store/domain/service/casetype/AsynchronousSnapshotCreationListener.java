package uk.gov.hmcts.ccd.definition.store.domain.service.casetype;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.gov.hmcts.ccd.definition.store.event.SnapshotCreationEvent;

@Service
@ConditionalOnProperty(
    name = "case-type.snapshot.enabled",
    havingValue = "true",
    matchIfMissing = true
)
@Slf4j
public class AsynchronousSnapshotCreationListener {

    private final SnapshotCreator snapshotCreator;

    public AsynchronousSnapshotCreationListener(SnapshotCreator snapshotCreator) {
        this.snapshotCreator = snapshotCreator;
    }

    @Async
    @TransactionalEventListener
    public void onSnapshotCreationRequested(SnapshotCreationEvent event) {
        String jurisdictionName = event.jurisdiction() != null ? event.jurisdiction() : "all";
        log.info("Starting asynchronous snapshot creation for {} case types from {} jurisdiction.",
            event.caseTypeReferences().size(), jurisdictionName);

        try {
            createSnapshots(event);

            log.info("Successfully completed snapshot creation for {} case types from {} jurisdiction",
                event.caseTypeReferences().size(),
                jurisdictionName);

        } catch (Exception e) {
            log.warn("Error creating snapshots - snapshots will be created on-demand.", e);
        }
    }

    private void createSnapshots(SnapshotCreationEvent event) {
        log.debug("Processing {} case types for snapshot creation", event.caseTypeReferences().size());

        int successCount = 0;
        int failureCount = 0;

        for (String caseTypeReference : event.caseTypeReferences()) {
            try {
                snapshotCreator.createSnapshotForCaseType(caseTypeReference);
                successCount++;
            } catch (Exception e) {
                log.warn("Failed to create snapshot for case type: {}", caseTypeReference, e);
                failureCount++;
            }
        }

        log.info("Snapshot creation completed. Success: {}, Failures: {}", successCount, failureCount);
    }
}
