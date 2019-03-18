package uk.gov.hmcts.ccd.definition.store.write.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

public interface CaseTypeWriteRepository extends VersionedWriteDefinitionRepository<CaseTypeEntity, Integer> {

    String SELECT_MAX_CASE_TYPE_VERSION_NUMBER =
        "select max(cm.version) from CaseTypeEntity as cm where cm.reference = :caseTypeReference";

    @Query(SELECT_MAX_CASE_TYPE_VERSION_NUMBER)
    Optional<Integer> findLastVersion(@Param("caseTypeReference") String caseTypeReference);

}
