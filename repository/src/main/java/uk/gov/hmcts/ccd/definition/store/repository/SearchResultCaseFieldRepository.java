package uk.gov.hmcts.ccd.definition.store.repository;

import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchResultCaseFieldEntity;

import java.util.List;

public interface SearchResultCaseFieldRepository extends DefinitionReadRepository<SearchResultCaseFieldEntity, Integer> {

    List<SearchResultCaseFieldEntity> findByCaseTypeId(Integer caseTypeId);
}
