package uk.gov.hmcts.ccd.definition.store.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.ccd.definition.store.repository.entity.RoleToAccessProfileEntity;

public interface RoleToAccessProfileRepository extends JpaRepository<RoleToAccessProfileEntity, Integer> {

    @Query("select ap from RoleToAccessProfileEntity ap where ap.caseType.reference in :references")
    List<RoleToAccessProfileEntity> findByCaseTypeReference(@Param("references") List<String> references);

    @Query("select ap from RoleToAccessProfileEntity ap where ap.roleName in :roleName")
    List<RoleToAccessProfileEntity> findByRoleNme(@Param("roleName") String roleName);

}
