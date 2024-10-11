package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRoleEntity;

import java.util.List;

public interface AccessTypeRolesRepository extends JpaRepository<AccessTypeRoleEntity, Integer> {

    @Query(value = "SELECT "
        + "atr.* "
        + "FROM access_type_role atr "
        + "INNER JOIN case_type ct1 "
        + "ON ct1.id = atr.case_type_id "
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
        + "ON ct1.id = ct2.id ", nativeQuery = true)
    List<AccessTypeRoleEntity> findAllWithCaseTypeIds();

    @Query(value = "SELECT "
        + "atr.* "
        + "FROM access_type_role atr "
        + "INNER JOIN case_type ct1 "
        + "ON ct1.id = atr.case_type_id "
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
        + "AND atr.organisation_profile_id in :organisationProfileIds", nativeQuery = true)
    List<AccessTypeRoleEntity> findByOrganisationProfileIds(List<String>  organisationProfileIds);

}
