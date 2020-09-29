package uk.gov.hmcts.ccd.definition.store.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.ccd.definition.store.repository.entity.NoCConfigEntity;

public interface NoCConfigRepository extends JpaRepository<NoCConfigEntity, Integer> {

    @Query("select b from NoCConfigEntity b where b.caseType.reference=:caseTypeReference")
    NoCConfigEntity findByCaseTypeReference(@Param("caseTypeReference") String caseTypeReference);

    @Query("select b from NoCConfigEntity b where b.caseType.reference in :references")
    List<NoCConfigEntity> findAllByCaseTypeReferences(@Param("references") List<String> references);

    @Query("select b from NoCConfigEntity b")
    List<NoCConfigEntity> findAll();

    @Modifying
    @Query("delete from NoCConfigEntity b where b.id in (select b.id from NoCConfigEntity b where "
        + "b.caseType.reference=:caseTypeReference)")
    int deleteByCaseTypeReference(@Param("caseTypeReference") String reference);
}
