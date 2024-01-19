package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRolesEntity;

import java.util.List;

public interface AccessTypeRolesRepository extends JpaRepository<AccessTypeRolesEntity, Integer> {

    @Query("select atr from AccessTypeRolesEntity atr where"
        + " atr.caseTypeId.version = (select max(ct.version) from CaseTypeEntity ct)"
        + " and atr.reference = ct1.reference")
    List<AccessTypeRolesEntity> findAllWithCaseTypeIds();

    @Query("select atr from AccessTypeRolesEntity atr, CaseTypeEntity ct1 where atr.organisationProfileId"
        + " in :organisationProfileIds"
        + " and atr.caseTypeId.version = (select max(ct.version) from CaseTypeEntity ct)"
        + " and atr.reference = ct1.reference")
    List<AccessTypeRolesEntity> findByOrganisationProfileIds(List<String>  organisationProfileIds);

}
