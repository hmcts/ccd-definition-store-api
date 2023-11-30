package uk.gov.hmcts.ccd.definition.store.domain.service.accesstyperoles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.AccessTypeRolesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRolesEntity;

import java.util.List;

@Component
public class AccessTypeRolesServiceImpl implements AccessTypeRolesService {

    private static final Logger LOG = LoggerFactory.getLogger(AccessTypeRolesServiceImpl.class);

    private final AccessTypeRolesRepository repository;

    @Autowired
    public AccessTypeRolesServiceImpl(AccessTypeRolesRepository repository) {
        this.repository = repository;
    }

    @Override
    public void saveAll(List<AccessTypeRolesEntity> entityList) {
        repository.saveAll(entityList);
    }
}
