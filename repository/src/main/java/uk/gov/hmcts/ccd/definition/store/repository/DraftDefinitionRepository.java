package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DefinitionEntity;

import java.util.List;
import java.util.Optional;

public interface DraftDefinitionRepository extends DefinitionRepository<DefinitionEntity, Integer> {

    @Query("SELECT MAX(d.version) FROM DefinitionEntity d WHERE d.jurisdiction.reference = :jurisdictionReference")
    Optional<Integer> findLastVersion(@Param("jurisdictionReference") String jurisdiction);

    @Query("SELECT d FROM DefinitionEntity d WHERE d.version = (SELECT MAX(dm.version) FROM DefinitionEntity dm"
        + " WHERE dm.jurisdiction.reference = :jurisdictionReference)"
        + " AND d.jurisdiction.reference = :jurisdictionReference")
    DefinitionEntity findLatestByJurisdictionId(@Param("jurisdictionReference") String jurisdiction);

    @Query("SELECT d FROM DefinitionEntity d"
        + " WHERE d.jurisdiction.reference = :jurisdictionReference order by d.id desc")
    List<DefinitionEntity> findByJurisdictionId(@Param("jurisdictionReference") String jurisdiction);

    @Query("SELECT d FROM DefinitionEntity d WHERE d.version = :version"
        + " AND d.jurisdiction.reference = :jurisdictionReference")
    DefinitionEntity findByJurisdictionIdAndVersion(@Param("jurisdictionReference") String jurisdiction,
                                                    @Param("version") Integer version);
}
