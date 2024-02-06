package uk.gov.hmcts.ccd.definition.store.domain.service.accesstyperoles;

import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRoleEntity;

import java.util.List;

public interface AccessTypeRolesService {

    void saveAll(List<AccessTypeRoleEntity> entityList);
}
