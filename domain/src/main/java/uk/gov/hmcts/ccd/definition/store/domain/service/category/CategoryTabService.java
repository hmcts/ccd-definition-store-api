package uk.gov.hmcts.ccd.definition.store.domain.service.category;

import uk.gov.hmcts.ccd.definition.store.repository.entity.CategoryEntity;

import java.util.List;

public interface CategoryTabService {

    void saveAll(List<CategoryEntity> entity);
}
