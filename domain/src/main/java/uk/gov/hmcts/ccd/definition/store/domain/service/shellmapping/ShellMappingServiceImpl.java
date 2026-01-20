package uk.gov.hmcts.ccd.definition.store.domain.service.shellmapping;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.ShellMappingRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ShellMappingEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.ShellMapping;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ShellMappingServiceImpl implements ShellMappingService {

    private ShellMappingRepository shellMappingRepository;

    private final EntityToResponseDTOMapper dtoMapper;

    public ShellMappingServiceImpl(ShellMappingRepository repository,
                                   EntityToResponseDTOMapper dtoMapper) {
        this.shellMappingRepository = repository;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public void saveAll(List<ShellMappingEntity> entityList) {
        shellMappingRepository.saveAll(entityList);
    }

    @Override
    public List<ShellMapping> findAll() {
        List<ShellMappingEntity> shellMappingEntities = shellMappingRepository.findAll();
        return shellMappingEntities.stream()
            .map(dtoMapper::map)
            .collect(Collectors.toList());
    }
}
