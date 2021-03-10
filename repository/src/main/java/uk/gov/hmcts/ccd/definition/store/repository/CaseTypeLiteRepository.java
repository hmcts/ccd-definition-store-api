package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity;

import java.util.List;
import java.util.Optional;

public interface CaseTypeLiteRepository extends DefinitionRepository<CaseTypeLiteEntity, Integer> {

    @Query("SELECT c FROM CaseTypeLiteEntity c WHERE c.version IN (SELECT MAX(cm.version) FROM CaseTypeLiteEntity cm "
        + "WHERE cm.reference = c.reference) AND c.jurisdiction.reference = :jurisdictionReference")
    List<CaseTypeLiteEntity> findByJurisdictionId(@Param("jurisdictionReference") String jurisdiction);

    @Query("select c from CaseTypeLiteEntity c where c.reference=:caseTypeReference "
        + "and c.version in (select max(cm.version) from CaseTypeEntity as cm where cm.reference = :caseTypeReference)")
    Optional<CaseTypeLiteEntity> findCurrentVersionForReference(@Param("caseTypeReference") String caseTypeReference);

}
