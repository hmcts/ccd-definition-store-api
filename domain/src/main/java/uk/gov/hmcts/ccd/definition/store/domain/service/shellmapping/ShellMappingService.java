package uk.gov.hmcts.ccd.definition.store.domain.service.shellmapping;

import uk.gov.hmcts.ccd.definition.store.repository.entity.ShellMappingEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.ShellMapping;

import java.util.List;

public interface ShellMappingService {

    void saveAll(List<ShellMappingEntity> entityList);

    List<ShellMapping> findAll();
}
