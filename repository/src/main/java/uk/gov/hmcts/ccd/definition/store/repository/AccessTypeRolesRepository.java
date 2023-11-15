package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRolesEntity;

import java.util.List;

public interface AccessTypeRolesRepository extends JpaRepository<AccessTypeRolesEntity, Integer> {

    @Query("select atr from AccessTypeRolesEntity atr where atr.organisationProfileID in (:organisationProfileIds) "
        + "and atr.caseType.version = "
        + "(select max(ct.version) from CaseTypeEntity ct where ct.reference=atr.caseType.reference)")
    List<AccessTypeRolesEntity> findByOrganisationProfileIDs(
        @Param("organisationProfileIds") List<String> organisationProfileIds);

}
