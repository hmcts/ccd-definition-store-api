package uk.gov.hmcts.ccd.definition.store.domain.service.accesstypes;

import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeEntity;

import java.util.List;

public interface AccessTypesService {

    void saveAll(List<AccessTypeEntity> entityList);
}
