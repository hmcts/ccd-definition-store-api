package uk.gov.hmcts.ccd.definition.store.domain.service.accessprofiles;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.RoleToAccessProfileRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.RoleToAccessProfileEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.RoleToAccessProfile;

@Component
public class RoleToAccessProfileServiceImpl implements RoleToAccessProfileService {

    private final RoleToAccessProfileRepository repository;

    private final EntityToResponseDTOMapper dtoMapper;

    @Autowired
    public RoleToAccessProfileServiceImpl(RoleToAccessProfileRepository repository,
                                          EntityToResponseDTOMapper dtoMapper) {
        this.repository = repository;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public void saveAll(List<RoleToAccessProfileEntity> entityList) {
        repository.saveAll(entityList);
    }

    @Override
    public List<RoleToAccessProfile> findByRoleName(String roleName) {
        List<RoleToAccessProfileEntity> roleToAccessProfileEntities = repository
            .findByRoleNme(roleName);
        return roleToAccessProfileEntities.stream()
            .map(dtoMapper::map)
            .collect(Collectors.toList());
    }

    @Override
    public List<RoleToAccessProfile> findByCaseTypeReferences(List<String> caseTypeReferences) {
        List<RoleToAccessProfileEntity> roleToAccessProfileEntities = repository
            .findByCaseTypeReference(caseTypeReferences);
        return roleToAccessProfileEntities.stream()
            .map(dtoMapper::map)
            .collect(Collectors.toList());
    }
}
