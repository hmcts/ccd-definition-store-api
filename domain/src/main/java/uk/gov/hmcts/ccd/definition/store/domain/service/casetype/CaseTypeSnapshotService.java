package uk.gov.hmcts.ccd.definition.store.domain.service.casetype;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeSnapshotRepository;
import uk.gov.hmcts.ccd.definition.store.repository.JsonUtils;
import uk.gov.hmcts.ccd.definition.store.repository.SnapshotJdbcRepository;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;

import java.util.Optional;

@Service
@Slf4j
public class CaseTypeSnapshotService {

    private final CaseTypeSnapshotRepository snapshotRepository;
    private final SnapshotJdbcRepository snapshotJdbcRepository;

    public CaseTypeSnapshotService(CaseTypeSnapshotRepository snapshotRepository,
                                   SnapshotJdbcRepository snapshotJdbcRepository) {
        this.snapshotRepository = snapshotRepository;
        this.snapshotJdbcRepository = snapshotJdbcRepository;
    }

    /**
     * Retrieve a snapshot case type for a specific version
     *
     * @param caseTypeReference the case type reference
     * @param version the specific version to look for
     * @return cached CaseType if found and valid, empty otherwise
     */
    public Optional<CaseType> getSnapshot(String caseTypeReference, Integer version) {
        log.debug("Looking for cached response for case type: {} version: {}", caseTypeReference, version);

        return snapshotJdbcRepository.loadCaseTypeSnapshot(caseTypeReference, version);
    }

    /**
     * store snapshot for given case type and version
     *
     * @param caseTypeReference the case type reference
     * @param version the version to cache
     * @param caseType the caseType object to cache
     */
    public void storeSnapshot(String caseTypeReference, Integer version, CaseType caseType) {
        try {
            log.debug("Storing the caseType snapshot for case type: {} version: {}", caseTypeReference, version);

            String serializedResponse = JsonUtils.toString(caseType);
            if (Strings.isNullOrEmpty(serializedResponse)) {
                log.warn("Serialization produced empty result for case type: {} version: {}",
                    caseTypeReference, version);
                return;
            }

            snapshotRepository.upsertSnapshot(caseTypeReference, version, serializedResponse);

            log.info("Successfully stored caseType snapshot for case type: {} version: {}", caseTypeReference,
                version);
        } catch (Exception e) {
            log.warn("Failed to store caseType snapshot for case type: {} version: {}",
                caseTypeReference, version, e);
        }
    }
}
