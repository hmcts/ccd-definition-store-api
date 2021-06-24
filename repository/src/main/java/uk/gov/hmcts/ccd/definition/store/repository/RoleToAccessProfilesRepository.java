package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.ccd.definition.store.repository.entity.RoleToAccessProfilesEntity;

import java.util.List;

import static uk.gov.hmcts.ccd.definition.store.repository.QueryConstants.SELECT_LATEST_CASE_TYPE_ENTITY_FOR_REFERENCE;

public interface RoleToAccessProfilesRepository extends JpaRepository<RoleToAccessProfilesEntity, Integer> {

    List<RoleToAccessProfilesEntity> findByRoleName(String roleName);

    List<RoleToAccessProfilesEntity> findByCaseTypeReferenceIn(List<String> caseTypeReferences);

    @Query("select cre from RoleToAccessProfilesEntity cre where cre.caseType = ("
        + SELECT_LATEST_CASE_TYPE_ENTITY_FOR_REFERENCE + ")")
    List<RoleToAccessProfilesEntity> findRoleToAccessProfilesEntityByCaseType(
        @Param("caseTypeReference") String caseType
    );

}
