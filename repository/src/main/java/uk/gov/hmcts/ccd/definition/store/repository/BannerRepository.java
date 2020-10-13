package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.ccd.definition.store.repository.entity.BannerEntity;

import java.util.List;

public interface BannerRepository extends JpaRepository<BannerEntity, Integer> {

    @Query("select b from BannerEntity b where b.jurisdiction.reference=:jurisdictionReference")
    BannerEntity findByJurisdictionId(@Param("jurisdictionReference") String jurisdiction);

    @Query("select b from BannerEntity b where b.jurisdiction.reference in :references")
    List<BannerEntity> findAllByReference(@Param("references") List<String> references);

    @Query("select b from BannerEntity b")
    List<BannerEntity> findAll();

    @Modifying
    @Query("delete from BannerEntity b where b.id in (select b.id from BannerEntity b where "
        + "b.jurisdiction.reference=:jurisdictionReference)")
    int deleteByJurisdictionReference(@Param("jurisdictionReference") String jurisdiction);
}
