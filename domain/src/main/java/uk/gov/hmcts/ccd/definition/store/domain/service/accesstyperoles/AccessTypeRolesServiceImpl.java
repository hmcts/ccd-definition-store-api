package uk.gov.hmcts.ccd.definition.store.domain.service.accesstyperoles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.AccessTypeRolesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRoleEntity;

import java.util.List;

@Component
public class AccessTypeRolesServiceImpl implements AccessTypeRolesService {

    private final AccessTypeRolesRepository repository;

    @Autowired
    public AccessTypeRolesServiceImpl(AccessTypeRolesRepository repository) {
        this.repository = repository;
    }

    @Override
    public void saveAll(List<AccessTypeRoleEntity> entityList) {
        repository.saveAll(entityList);
    }
}
