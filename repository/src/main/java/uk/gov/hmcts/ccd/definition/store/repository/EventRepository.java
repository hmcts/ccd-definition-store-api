package uk.gov.hmcts.ccd.definition.store.repository;

import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import java.util.List;

public interface EventRepository extends DefinitionRepository<EventEntity, Integer> {

    List<EventEntity> findByReferenceAndCaseTypeId(@Param("reference")String eventReference, @Param("caseTypeId")Integer caseTypeId);

}
