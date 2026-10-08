package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeSnapshotEntity;

public interface CaseTypeSnapshotRepository extends DefinitionRepository<CaseTypeSnapshotEntity, Integer> {

    /**
     * Check if snapshot exists for given case type and version.
     */
    boolean existsByCaseTypeReferenceAndVersionId(String caseTypeReference, Integer versionId);

    /**
     * Upsert snapshot - Insert a new record or update an existing one.
     */
    default void upsertSnapshot(String caseTypeReference, Integer versionId, String precomputedResponse) {
        upsertSnapshot(caseTypeReference, versionId, precomputedResponse, SnapshotFormat.REVISION);
    }

    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query(value = """
        INSERT INTO case_type_snapshot
                    (case_type_reference, version_id, precomputed_response, format_revision,
                     format_write_count, created_at, last_modified)
        VALUES (:caseTypeReference, :versionId, CAST(:precomputedResponse AS jsonb), :formatRevision, 1, NOW(), NOW())
        ON CONFLICT (case_type_reference)
        DO UPDATE SET
            version_id = EXCLUDED.version_id,
            precomputed_response = EXCLUDED.precomputed_response,
            format_revision = EXCLUDED.format_revision,
            format_write_count = case_type_snapshot.format_write_count + 1,
            last_modified = NOW()
        WHERE case_type_snapshot.version_id <= EXCLUDED.version_id
          AND case_type_snapshot.format_revision <= EXCLUDED.format_revision
        """, nativeQuery = true)
    void upsertSnapshot(@Param("caseTypeReference") String caseTypeReference,
                        @Param("versionId") Integer versionId,
                        @Param("precomputedResponse") String precomputedResponse,
                        @Param("formatRevision") int formatRevision);

}
