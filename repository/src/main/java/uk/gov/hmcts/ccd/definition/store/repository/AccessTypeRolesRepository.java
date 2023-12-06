package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRolesEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.OrganisationProfileIds;

import java.util.List;

public interface AccessTypeRolesRepository extends JpaRepository<AccessTypeRolesEntity, Integer> {

    @Query("select atr from AccessTypeRolesEntity atr inner join fetch atr.caseTypeId")
    List<AccessTypeRolesEntity> findAllWithCaseTypeIds();

    @Query("select atr from AccessTypeRolesEntity atr inner join fetch atr.caseTypeId where atr.organisation_profile_id in :organisationProfileIds"
           + "and atr.caseType.version = (select max(ct..version) from CaseTypeEntity ct atr inner join where ct.reference=atr.caseType.reference)")
    List<AccessTypeRolesEntity> findByOrganisationProfileIds(List<String>  organisationProfileIds);
}
