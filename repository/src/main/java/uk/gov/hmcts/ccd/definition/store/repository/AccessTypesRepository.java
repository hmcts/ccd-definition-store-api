package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeEntity;

import java.util.List;

public interface AccessTypesRepository extends JpaRepository<AccessTypeEntity, Integer> {

    @Query(value = "SELECT "
        + "at.* "
        + "FROM   access_type at "
        + "INNER JOIN case_type ct1 "
        + "ON ct1.id = at.case_type_id "
        + "INNER JOIN ( "
        + "SELECT  "
        + "ct.id,ct.description,ct.jurisdiction_id,ct.name,ct.reference AS reference,ct.version "
        + "FROM  case_type ct "
        + "INNER JOIN jurisdiction j "
        + "ON ct.jurisdiction_id = j.id "
        + "INNER JOIN ("
        + "SELECT reference,MAX(version) AS max_version "
        + "FROM case_type "
        + "GROUP BY reference) max_versions "
        + "ON ct.reference = max_versions.reference "
        + "AND ct.version = max_versions.max_version ) AS ct2 "
        + "ON ct1.id = ct2.id", nativeQuery = true)
    List<AccessTypeEntity> findAllWithCaseTypeIds();

    @Query(value = "SELECT "
        + "at.* "
        + "FROM   access_type at "
        + "INNER JOIN case_type ct1 "
        + "ON ct1.id = at.case_type_id "
        + "INNER JOIN ( "
        + "SELECT  "
        + "ct.id,ct.description,ct.jurisdiction_id,ct.name,ct.reference AS reference,ct.version "
        + "FROM  case_type ct "
        + "INNER JOIN jurisdiction j "
        + "ON ct.jurisdiction_id = j.id "
        + "INNER JOIN ("
        + "SELECT reference,MAX(version) AS max_version "
        + "FROM case_type "
        + "GROUP BY reference) max_versions "
        + "ON ct.reference = max_versions.reference "
        + "AND ct.version = max_versions.max_version ) AS ct2 "
        + "ON ct1.id = ct2.id "
        + "AND at.organisation_profile_id in :organisationProfileIds", nativeQuery = true)
    List<AccessTypeEntity> findByOrganisationProfileIds(List<String>  organisationProfileIds);

}
