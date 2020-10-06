package uk.gov.hmcts.ccd.definition.store.repository;

import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchInputCaseFieldEntity;

import java.util.List;

public interface SearchInputCaseFieldRepository extends DefinitionRepository<SearchInputCaseFieldEntity, Integer> {

    List<SearchInputCaseFieldEntity> findByCaseTypeId(Integer caseTypeId);

}
