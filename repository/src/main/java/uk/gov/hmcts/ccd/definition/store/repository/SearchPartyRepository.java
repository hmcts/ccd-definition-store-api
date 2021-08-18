package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchPartyEntity;

import java.util.List;

import static uk.gov.hmcts.ccd.definition.store.repository.QueryConstants.SELECT_LATEST_CASE_TYPE_ENTITY_FOR_REFERENCE;

public interface SearchPartyRepository extends JpaRepository<SearchPartyEntity, Integer> {

    List<SearchPartyEntity> findBySearchPartyName(String searchPartyName);

    List<SearchPartyEntity> findByCaseTypeReferenceIn(List<String> caseTypeReferences);

    @Query("select cre from SearchPartyEntity cre where cre.caseType = ("
        + SELECT_LATEST_CASE_TYPE_ENTITY_FOR_REFERENCE + ")")
    List<SearchPartyEntity> findSearchPartyEntityByCaseType(
        @Param("caseTypeReference") String caseType
    );

}
