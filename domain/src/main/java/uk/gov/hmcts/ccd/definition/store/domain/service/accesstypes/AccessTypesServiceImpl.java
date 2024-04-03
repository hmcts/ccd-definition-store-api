package uk.gov.hmcts.ccd.definition.store.domain.service.accesstypes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.AccessTypesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeEntity;

import java.util.List;

@Component
public class AccessTypesServiceImpl implements AccessTypesService {

    private final AccessTypesRepository repository;

    @Autowired
    public AccessTypesServiceImpl(AccessTypesRepository repository) {
        this.repository = repository;
    }

    @Override
    public void saveAll(List<AccessTypeEntity> entityList) {
        repository.saveAll(entityList);
    }
}
