package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionUiConfigEntity;

import java.util.List;

public interface JurisdictionUiConfigRepository extends JpaRepository<JurisdictionUiConfigEntity, Integer> {

    @Query("select j from JurisdictionUiConfigEntity j where j.jurisdiction.reference=:jurisdictionReference")
    JurisdictionUiConfigEntity findByJurisdictionId(@Param("jurisdictionReference") String jurisdiction);

    @Query("select j from JurisdictionUiConfigEntity j where j.jurisdiction.reference in :references")
    List<JurisdictionUiConfigEntity> findAllByReference(@Param("references") List<String> references);

}
