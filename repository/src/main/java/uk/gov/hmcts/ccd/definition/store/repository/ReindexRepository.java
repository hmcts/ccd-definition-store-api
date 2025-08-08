package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessProfileEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ReindexEntity;

import java.util.List;

@Repository
public interface ReindexRepository extends JpaRepository<ReindexEntity, Integer> {

    @Query(
        value = "SELECT * FROM reindex",
        nativeQuery = true)
    List<AccessProfileEntity> getAll();

    List<ReindexEntity> findByCaseType(String caseType);
}
