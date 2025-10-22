package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeSnapshotEntity;

public interface CaseTypeSnapshotRepository extends DefinitionRepository<CaseTypeSnapshotEntity, Integer> {

    /**
     * Upsert snapshot - Insert new record or update existing one
     */
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query(value = """
        INSERT INTO case_type_snapshot (case_type_reference, version_id, precomputed_response, created_at, last_modified)
        VALUES (:caseTypeReference, :versionId, CAST(:precomputedResponse AS jsonb), NOW(), NOW())
        ON CONFLICT (case_type_reference)
        DO UPDATE SET
            version_id = EXCLUDED.version_id,
            precomputed_response = EXCLUDED.precomputed_response,
            last_modified = NOW()
        WHERE case_type_snapshot.version_id < EXCLUDED.version_id
        """, nativeQuery = true)
    void upsertSnapshot(@Param("caseTypeReference") String caseTypeReference,
                        @Param("versionId") Integer versionId,
                        @Param("precomputedResponse") String precomputedResponse);

}
