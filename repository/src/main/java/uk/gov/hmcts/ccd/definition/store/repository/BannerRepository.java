package uk.gov.hmcts.ccd.definition.store.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.ccd.definition.store.repository.entity.BannerEntity;

public interface BannerRepository extends JpaRepository<BannerEntity, Integer> {

    @Query("select b from BannerEntity b where b.jurisdiction.reference=:jurisdictionReference")
    List<BannerEntity> findByJurisdictionId(@Param("jurisdictionReference") String jurisdiction);
}
