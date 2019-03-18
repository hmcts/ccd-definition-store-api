package uk.gov.hmcts.ccd.definition.store.write.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

public interface JurisdictionWriteRepository extends VersionedWriteDefinitionRepository<JurisdictionEntity, Integer> {

    @Query("select max(entity.version) from JurisdictionEntity as entity where entity.reference = :reference")
    Optional<Integer> findLastVersion(@Param("reference") String reference);

}
