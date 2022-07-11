package uk.gov.hmcts.ccd.definition.store.repository;

import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ADDRESS_GLOBAL;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ADDRESS_GLOBAL_UK;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ADDRESS_UK;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_CASELINK;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_CASE_LOCATION;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_CHANGE_ORGANISATION_REQUEST;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_FLAGS;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_LINK_REASON;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ORDER_SUMMARY;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ORGANISATION;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ORGANISATION_POLICY;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_PREVIOUS_ORGANISATION;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_TTL;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_SEARCH_CRITERIA;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_SEARCH_PARTY;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.List;
import java.util.Optional;

public interface FieldTypeRepository extends VersionedDefinitionRepository<FieldTypeEntity, Integer> {

    String FIND_BASE_TYPES_QUERY =
        "select entity from FieldTypeEntity as entity where entity.baseFieldType is null and entity.version = "
            + "(select max(e2.version) from FieldTypeEntity as e2 where e2.reference = entity.reference)";

    @Override
    @Query("select max(entity.version) from FieldTypeEntity as entity where entity.reference = :reference")
    Optional<Integer> findLastVersion(@Param("reference") String reference);

    @Query(FIND_BASE_TYPES_QUERY)
    List<FieldTypeEntity> findCurrentBaseTypes();

    @Query(FIND_BASE_TYPES_QUERY + " and entity.reference = :reference")
    Optional<FieldTypeEntity> findBaseType(@Param("reference") String reference);

    @Query("select entity from FieldTypeEntity entity where entity.reference in ("
        + "'" + PREDEFINED_COMPLEX_ADDRESS_GLOBAL
        + "', '" + PREDEFINED_COMPLEX_ADDRESS_GLOBAL_UK
        + "', '" + PREDEFINED_COMPLEX_ADDRESS_UK
        + "', '" + PREDEFINED_COMPLEX_ORDER_SUMMARY
        + "', '" + PREDEFINED_COMPLEX_CASELINK
        + "', '" + PREDEFINED_COMPLEX_ORGANISATION
        + "', '" + PREDEFINED_COMPLEX_ORGANISATION_POLICY
        + "', '" + PREDEFINED_COMPLEX_CHANGE_ORGANISATION_REQUEST
        + "', '" + PREDEFINED_COMPLEX_PREVIOUS_ORGANISATION
        + "', '" + PREDEFINED_COMPLEX_CASE_LOCATION
        + "', '" + PREDEFINED_COMPLEX_FLAGS
        + "', '" + PREDEFINED_COMPLEX_SEARCH_PARTY
        + "', '" + PREDEFINED_COMPLEX_SEARCH_CRITERIA
        + "', '" + PREDEFINED_COMPLEX_LINK_REASON
        + "', '" + PREDEFINED_COMPLEX_TTL
        + "')")
    List<FieldTypeEntity> findPredefinedComplexTypes();
}
