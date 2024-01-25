package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeEntity;

import java.util.List;

public interface AccessTypesRespository extends JpaRepository<AccessTypeEntity, Integer> {

    @Query("select atr from AccessTypeEntity atr, CaseTypeEntity ct where"
        + " atr.caseTypeId.version "
        + " = (select max(ct1.version) from CaseTypeEntity ct1, AccessTypeEntity atr1 where"
        + " ct1.id=atr1.caseTypeId)"
        + " and atr.caseTypeId=ct.id")
    List<AccessTypeEntity> findAllWithCaseTypeIds();

    @Query("select atr from AccessTypeEntity atr, CaseTypeEntity ct where "
        + " atr.organisationProfileId in :organisationProfileIds"
        + " and atr.caseTypeId.version "
        + " = (select max(ct1.version) from CaseTypeEntity ct1, AccessTypeEntity atr1 where"
        + " ct1.id=atr1.caseTypeId)"
        + " and atr.caseTypeId=ct.id")
    List<AccessTypeEntity> findByOrganisationProfileIds(List<String>  organisationProfileIds);

}
