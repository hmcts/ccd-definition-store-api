package uk.gov.hmcts.ccd.definition.store.domain.service.accessprofiles;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.RoleToAccessProfilesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.RoleToAccessProfilesEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.RoleToAccessProfiles;

@Component
public class RoleToAccessProfilesServiceImpl implements RoleToAccessProfileService {

    private final RoleToAccessProfilesRepository repository;

    private final EntityToResponseDTOMapper dtoMapper;

    @Autowired
    public RoleToAccessProfilesServiceImpl(RoleToAccessProfilesRepository repository,
                                           EntityToResponseDTOMapper dtoMapper) {
        this.repository = repository;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public void saveAll(List<RoleToAccessProfilesEntity> entityList) {
        repository.saveAll(entityList);
    }

    @Override
    public List<RoleToAccessProfiles> findByRoleName(String roleName) {
        List<RoleToAccessProfilesEntity> roleToAccessProfileEntities = repository
            .findByRoleNme(roleName);
        return roleToAccessProfileEntities.stream()
            .map(dtoMapper::map)
            .collect(Collectors.toList());
    }

    @Override
    public List<RoleToAccessProfiles> findByCaseTypeReferences(List<String> caseTypeReferences) {
        List<RoleToAccessProfilesEntity> roleToAccessProfileEntities = repository
            .findByCaseTypeReference(caseTypeReferences);
        return roleToAccessProfileEntities.stream()
            .map(dtoMapper::map)
            .collect(Collectors.toList());
    }
}
