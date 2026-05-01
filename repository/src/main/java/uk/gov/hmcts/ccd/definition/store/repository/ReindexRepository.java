package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ReindexEntity;

import java.util.List;
import java.util.Optional;

public interface ReindexRepository extends JpaRepository<ReindexEntity, Integer> {

    List<ReindexEntity> findByCaseType(String caseType);

    Page<ReindexEntity> findByCaseType(String caseType, Pageable pageable);

    Optional<ReindexEntity> findByIndexName(String indexName);
}
