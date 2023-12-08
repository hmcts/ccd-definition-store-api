package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRolesEntity;

import java.util.List;

public interface AccessTypeRolesRepository extends JpaRepository<AccessTypeRolesEntity, Integer> {

    @Query("select atr from AccessTypeRolesEntity atr inner join fetch atr.caseTypeId")
    List<AccessTypeRolesEntity> findAllWithCaseTypeIds();

    @Query("select atr from AccessTypeRolesEntity atr where atr.organisationProfileId in :organisationProfileIds"
        + " and atr.caseTypeId.version = (select max(ct.version) from CaseTypeEntity ct where ct.id=atr.caseTypeId)")
    List<AccessTypeRolesEntity> findByOrganisationProfileIds(List<String>  organisationProfileIds);
}
