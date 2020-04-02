package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import java.util.List;
import java.util.Optional;

public interface JurisdictionRepository extends VersionedDefinitionRepository<JurisdictionEntity, Integer> {

    @Query("select max(entity.version) from JurisdictionEntity as entity where entity.reference = :reference")
    Optional<Integer> findLastVersion(@Param("reference") String reference);

    @Query("select j from JurisdictionEntity j where lower(j.reference) in :references and j.version = (" +
            "select max(j2.version) from JurisdictionEntity j2 where lower(j2.reference) = lower(j.reference)) ")
    List<JurisdictionEntity> findAllLatestVersionByReference(@Param("references") List<String> references);

    @Query("select j from JurisdictionEntity j where j.version = (" +
            "select max(j2.version) from JurisdictionEntity j2 where j2.reference = j.reference) ")
    List<JurisdictionEntity> findAllLatestVersion();
}
