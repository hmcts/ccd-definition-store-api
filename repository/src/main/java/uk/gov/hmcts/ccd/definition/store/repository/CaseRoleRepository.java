package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;

import java.util.List;

import static uk.gov.hmcts.ccd.definition.store.repository.QueryConstants.SELECT_LATEST_CASE_TYPE_ENTITY_FOR_REFERENCE;

public interface CaseRoleRepository extends JpaRepository<CaseRoleEntity, Integer> {

    @Query("select cre from CaseRoleEntity cre where cre.caseType = ("
        + SELECT_LATEST_CASE_TYPE_ENTITY_FOR_REFERENCE + ")")
    List<CaseRoleEntity> findCaseRoleEntitiesByCaseType(@Param("caseTypeReference") String caseType);

}
