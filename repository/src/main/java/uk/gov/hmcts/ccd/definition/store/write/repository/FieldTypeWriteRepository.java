package uk.gov.hmcts.ccd.definition.store.write.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

public interface FieldTypeWriteRepository extends VersionedWriteDefinitionRepository<FieldTypeEntity, Integer> {

    @Query("select max(entity.version) from FieldTypeEntity as entity where entity.reference = :reference")
    Optional<Integer> findLastVersion(@Param("reference") String reference);
}
