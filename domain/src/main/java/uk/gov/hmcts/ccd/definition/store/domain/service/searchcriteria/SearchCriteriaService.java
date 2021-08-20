package uk.gov.hmcts.ccd.definition.store.domain.service.searchcriteria;

import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchCriteriaEntity;

import java.util.List;

public interface SearchCriteriaService {

    void saveAll(List<SearchCriteriaEntity> entityList);

}
