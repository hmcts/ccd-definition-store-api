package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ReindexEntity;

import java.util.List;
import java.util.Optional;

public interface ReindexRepository extends JpaRepository<ReindexEntity, Integer> {

    List<ReindexEntity> findByCaseType(String caseType);

    Optional<ReindexEntity> findByIndexName(String indexName);
}
