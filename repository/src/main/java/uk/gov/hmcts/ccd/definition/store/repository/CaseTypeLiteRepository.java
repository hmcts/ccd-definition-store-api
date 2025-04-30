package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity;

import java.util.List;
import java.util.Optional;

public interface CaseTypeLiteRepository extends DefinitionRepository<CaseTypeLiteEntity, Integer> {

    @Query(value = "SELECT "
        + "ct.id, ct.description, ct.jurisdiction_id, ct.name, ct.reference AS reference, ct.version "
        + "FROM case_type ct "
        + "INNER JOIN ("
        + "    SELECT ct.reference, MAX(ct.version) AS max_version "
        + "    FROM case_type ct "
        + "    JOIN jurisdiction j "
        + "    ON j.id = ct.jurisdiction_id "
        + "    WHERE j.reference = :jurisdictionReference "
        + "    GROUP BY ct.reference"
        + ") max_versions "
        + "ON ct.reference = max_versions.reference "
        + "AND ct.version = max_versions.max_version", nativeQuery = true)
    List<CaseTypeLiteEntity> findByJurisdictionId(@Param("jurisdictionReference") String jurisdiction);

    @Query("select c from CaseTypeLiteEntity c where c.reference=:caseTypeReference "
        + "and c.version in (select max(cm.version) from CaseTypeEntity as cm where cm.reference = :caseTypeReference)")
    Optional<CaseTypeLiteEntity> findCurrentVersionForReference(@Param("caseTypeReference") String caseTypeReference);

}
