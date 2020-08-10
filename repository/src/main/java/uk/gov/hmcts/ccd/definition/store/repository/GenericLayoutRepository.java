package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.ccd.definition.store.repository.entity.*;

import java.util.List;

import static uk.gov.hmcts.ccd.definition.store.repository.QueryConstants.SELECT_LATEST_CASE_TYPE_ENTITY_FOR_REFERENCE;

public interface GenericLayoutRepository extends DefinitionRepository<GenericLayoutEntity, Integer> {

    @Query("select wbicf from WorkBasketInputCaseFieldEntity wbicf where wbicf.caseType = (" + SELECT_LATEST_CASE_TYPE_ENTITY_FOR_REFERENCE + ")")
    List<WorkBasketInputCaseFieldEntity> findWorkbasketInputByCaseTypeReference(@Param("caseTypeReference") String caseTypeReference);

    @Query("select wbcf from WorkBasketCaseFieldEntity wbcf where wbcf.caseType = (" + SELECT_LATEST_CASE_TYPE_ENTITY_FOR_REFERENCE + ")")
    List<WorkBasketCaseFieldEntity> findWorkbasketByCaseTypeReference(@Param("caseTypeReference") String caseTypeReference);

    @Query("select sicf from SearchInputCaseFieldEntity sicf where sicf.caseType = (" + SELECT_LATEST_CASE_TYPE_ENTITY_FOR_REFERENCE + ")")
    List<SearchInputCaseFieldEntity> findSearchInputsByCaseTypeReference(@Param("caseTypeReference") String caseTypeReference);

    @Query("select srcf from SearchResultCaseFieldEntity srcf where srcf.caseType = (" + SELECT_LATEST_CASE_TYPE_ENTITY_FOR_REFERENCE + ")")
    List<SearchResultCaseFieldEntity> findSearchResultsByCaseTypeReference(@Param("caseTypeReference") String caseTypeReference);

    @Query("select scrf from SearchCasesResultFieldEntity scrf where scrf.caseType = ("
        + SELECT_LATEST_CASE_TYPE_ENTITY_FOR_REFERENCE + ") and scrf.useCase =:useCase")
    List<SearchCasesResultFieldEntity> findSearchCasesResultsByCaseTypeReference(@Param("caseTypeReference") String caseTypeReference,
                                                                                 @Param("useCase") String useCase);

    @Query("select scrf from SearchCasesResultFieldEntity scrf where scrf.caseType = (" + SELECT_LATEST_CASE_TYPE_ENTITY_FOR_REFERENCE + ")")
    List<SearchCasesResultFieldEntity> findSearchCasesResultsByCaseTypeReference(@Param("caseTypeReference") String caseTypeReference);

}
