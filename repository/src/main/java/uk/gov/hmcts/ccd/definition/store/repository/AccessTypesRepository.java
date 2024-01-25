package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeEntity;

import java.util.List;

public interface AccessTypesRepository extends JpaRepository<AccessTypeEntity, Integer> {

    @Query("select atr from AccessTypeEntity atr, CaseTypeEntity ct, JurisdictionEntity j where"
        + " atr.caseTypeId.version = (select max(ct1.version) "
        + " from CaseTypeEntity ct1, AccessTypeEntity atr1 where "
        + " atr1.caseTypeId = ct1.id and ct1.jurisdiction = j.id)"
        + " and atr.caseTypeId=ct.id"
        + " and ct.jurisdiction = j.id")
    List<AccessTypeEntity> findAllWithCaseTypeIds();

    @Query("select atr from AccessTypeEntity atr, CaseTypeEntity ct, JurisdictionEntity j"
        + " where atr.organisationProfileId in :organisationProfileIds"
        + " and atr.caseTypeId.version = (select max(ct1.version) "
        + " from CaseTypeEntity ct1, AccessTypeEntity atr1 where "
        + " atr1.caseTypeId = ct1.id and ct1.jurisdiction = j.id)"
        + " and atr.caseTypeId=ct.id"
        + " and ct.jurisdiction = j.id")
    List<AccessTypeEntity> findByOrganisationProfileIds(List<String>  organisationProfileIds);

}
