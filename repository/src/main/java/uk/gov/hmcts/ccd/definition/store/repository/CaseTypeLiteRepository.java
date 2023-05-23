package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity;

import java.util.List;
import java.util.Optional;

public interface CaseTypeLiteRepository extends DefinitionRepository<CaseTypeLiteEntity, Integer> {

    @Query(value = "SELECT "
        + "ct.id,ct.description,ct.jurisdiction_id,ct.name,ct.reference AS reference,ct.version "
        + "FROM  case_type ct "
        + "INNER JOIN jurisdiction j "
        + "ON ct.jurisdiction_id = j.id "
        + "INNER JOIN ("
        + "SELECT reference,MAX(version) AS max_version "
        + "FROM case_type "
        + "GROUP BY reference) max_versions "
        + "ON ct.reference = max_versions.reference "
        + "AND ct.version = max_versions.max_version "
        + "WHERE j.reference = :jurisdictionReference", nativeQuery = true)
    List<CaseTypeLiteEntity> findByJurisdictionId(@Param("jurisdictionReference") String jurisdiction);

    @Query("select c from CaseTypeLiteEntity c where c.reference=:caseTypeReference "
        + "and c.version in (select max(cm.version) from CaseTypeEntity as cm where cm.reference = :caseTypeReference)")
    Optional<CaseTypeLiteEntity> findCurrentVersionForReference(@Param("caseTypeReference") String caseTypeReference);

}
