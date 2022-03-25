package uk.gov.hmcts.ccd.definition.store.domain.service.category;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.CategoryTabRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CategoryEntity;

import java.util.List;

@Component
public class CategoryTabServiceImpl implements CategoryTabService {
    private static final Logger LOG = LoggerFactory.getLogger(CategoryTabServiceImpl.class);

    private CategoryTabRepository categoryTabRepository;

    @Autowired
    public CategoryTabServiceImpl(CategoryTabRepository categoryTabRepository) {
        this.categoryTabRepository = categoryTabRepository;
    }

    @Override
    public void saveAll(List<CategoryEntity> entity) {
        LOG.debug("Create CategoryEntity Entity {}", entity);
        categoryTabRepository.saveAll(entity);
    }
}
