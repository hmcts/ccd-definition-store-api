package uk.gov.hmcts.ccd.definition.store.domain.service.accesstyperoles;

import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRolesEntity;

import java.util.List;

public interface AccessTypeRolesService {

    void saveAll(List<AccessTypeRolesEntity> entityList);
}