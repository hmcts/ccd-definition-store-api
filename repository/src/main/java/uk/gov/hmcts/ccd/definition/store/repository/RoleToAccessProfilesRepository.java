package uk.gov.hmcts.ccd.definition.store.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.ccd.definition.store.repository.entity.RoleToAccessProfilesEntity;

public interface RoleToAccessProfilesRepository extends JpaRepository<RoleToAccessProfilesEntity, Integer> {

    @Query("select ap from RoleToAccessProfilesEntity ap where ap.caseType.reference in :references")
    List<RoleToAccessProfilesEntity> findByCaseTypeReference(@Param("references") List<String> references);

    @Query("select ap from RoleToAccessProfilesEntity ap where ap.roleName in :roleName")
    List<RoleToAccessProfilesEntity> findByRoleNme(@Param("roleName") String roleName);

}
