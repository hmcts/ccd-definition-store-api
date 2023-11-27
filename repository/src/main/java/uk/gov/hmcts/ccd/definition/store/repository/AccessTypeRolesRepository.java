package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRolesEntity;

import java.util.List;

public interface AccessTypeRolesRepository extends JpaRepository<AccessTypeRolesEntity, Integer> {

    @Query("select atr from AccessTypeRolesEntity atr inner join fetch atr.caseTypeId")
    List<AccessTypeRolesEntity> findAllWithCaseTypeIds();

}
