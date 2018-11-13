package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DefinitionEntity;

import java.util.Optional;

public interface DraftDefinitionRepository extends DefinitionRepository<DefinitionEntity, Integer> {

    @Query("SELECT MAX(d.version) FROM DefinitionEntity d WHERE d.jurisdiction.reference = :jurisdictionReference")
    Optional<Integer> findLastVersion(@Param("jurisdictionReference") String jurisdiction);

    @Query("SELECT d FROM DefinitionEntity d WHERE d.version IN (SELECT MAX(dm.version) FROM DefinitionEntity dm "
        + "WHERE dm.id = d.id) AND d.jurisdiction.reference = :jurisdictionReference")
    DefinitionEntity findLatestByJurisdictionId(@Param("jurisdictionReference") String jurisdiction);
}
