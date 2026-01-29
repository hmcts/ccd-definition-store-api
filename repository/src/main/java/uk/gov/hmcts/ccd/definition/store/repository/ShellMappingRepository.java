package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ShellMappingEntity;

import java.util.List;

public interface ShellMappingRepository extends JpaRepository<ShellMappingEntity, Integer> {

    @Query("select sm from ShellMappingEntity sm where sm.originatingCaseTypeId.reference = :caseTypeReference")
    List<ShellMappingEntity> findByOriginatingCaseTypeIdReference(@Param("caseTypeReference")
                                                                  String caseTypeReference);

}
