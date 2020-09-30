package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupType;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import java.util.List;

import static uk.gov.hmcts.ccd.definition.store.repository.QueryConstants.SELECT_LATEST_CASE_TYPE_ENTITY_FOR_REFERENCE;

public interface DisplayGroupRepository extends DefinitionRepository<DisplayGroupEntity, Integer> {

    @Query("select dg from DisplayGroupEntity dg where dg.type = 'TAB' AND dg.purpose = 'VIEW' "
        + "AND dg.caseType = (" + SELECT_LATEST_CASE_TYPE_ENTITY_FOR_REFERENCE + ")")
    List<DisplayGroupEntity> findTabsByCaseTypeReference(@Param("caseTypeReference") String caseTypeReference);

    List<DisplayGroupEntity> findByTypeAndCaseTypeIdAndEventOrderByOrder(@Param("type") DisplayGroupType type,
                                                                         @Param("caseTypeId") Integer caseTypeId,
                                                                         @Param("event") EventEntity event);

}
