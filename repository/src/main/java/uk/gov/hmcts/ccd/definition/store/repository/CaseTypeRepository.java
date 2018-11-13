package uk.gov.hmcts.ccd.definition.store.repository;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.ccd.definition.store.repository.QueryConstants.SELECT_LATEST_CASE_TYPE_ENTITY_FOR_REFERENCE;
import static uk.gov.hmcts.ccd.definition.store.repository.QueryConstants.SELECT_MAX_CASE_TYPE_VERSION_NUMBER;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

public interface CaseTypeRepository extends VersionedDefinitionRepository<CaseTypeEntity, Integer> {

    @Query(SELECT_MAX_CASE_TYPE_VERSION_NUMBER)
    Optional<Integer> findLastVersion(@Param("caseTypeReference") String caseTypeReference);

    @Query(SELECT_LATEST_CASE_TYPE_ENTITY_FOR_REFERENCE)
    Optional<CaseTypeEntity> findCurrentVersionForReference(@Param("caseTypeReference") String caseTypeReference);

    @Query("select c from CaseTypeEntity c where c.version in (select max(cm.version) from CaseTypeEntity cm where cm.reference=c.reference) and c.jurisdiction.reference=:jurisdictionReference")
    List<CaseTypeEntity> findByJurisdictionId(@Param("jurisdictionReference") String jurisdiction);

    @Query("select count(c) from CaseTypeEntity c where c.reference=:caseTypeReference and c.jurisdiction.reference<>:excludedJurisdictionReference")
    Integer caseTypeExists(@Param("caseTypeReference") String caseTypeReference, @Param("excludedJurisdictionReference") String excludedJurisdictionReference);
}
