package uk.gov.hmcts.ccd.definition.store.domain.service.accessprofiles;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.domain.exception.NotFoundException;
import uk.gov.hmcts.ccd.definition.store.domain.service.CaseRoleServiceImpl;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.RoleToAccessProfilesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.RoleToAccessProfilesEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.RoleAssignment;
import uk.gov.hmcts.ccd.definition.store.repository.model.RoleToAccessProfiles;

import static java.util.stream.Collectors.toList;

@Component
public class RoleToAccessProfilesServiceImpl implements RoleToAccessProfileService {

    private static final Logger LOG = LoggerFactory.getLogger(CaseRoleServiceImpl.class);
    private final RoleToAccessProfilesRepository repository;
    private final EntityToResponseDTOMapper dtoMapper;
    private final CaseTypeRepository caseTypeRepository;


    @Autowired
    public RoleToAccessProfilesServiceImpl(RoleToAccessProfilesRepository repository,
                                           EntityToResponseDTOMapper dtoMapper, CaseTypeRepository caseTypeRepository) {
        this.repository = repository;
        this.dtoMapper = dtoMapper;
        this.caseTypeRepository = caseTypeRepository;
    }

    @Override
    public void saveAll(List<RoleToAccessProfilesEntity> entityList) {
        repository.saveAll(entityList);
    }

    @Override
    public List<RoleToAccessProfiles> findByRoleName(String roleName) {
        List<RoleToAccessProfilesEntity> roleToAccessProfileEntities = repository
            .findByRoleName(roleName);
        return roleToAccessProfileEntities.stream()
            .map(dtoMapper::map)
            .collect(Collectors.toList());
    }

    @Override
    public List<RoleToAccessProfiles> findByCaseTypeReferences(List<String> caseTypeReferences) {
        List<RoleToAccessProfilesEntity> roleToAccessProfileEntities = repository
            .findByCaseTypeReferenceIn(caseTypeReferences);
        return roleToAccessProfileEntities.stream()
            .map(dtoMapper::map)
            .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public List<RoleAssignment> findRoleAssignmentsByCaseTypeId(String caseType) {
        final var caseTypeVersion = caseTypeRepository.findLastVersion(caseType)
            .orElseThrow(() -> new NotFoundException(caseType));
        LOG.debug("CaseType version {} found. for caseType {}...", caseTypeVersion, caseType);

        final List<RoleToAccessProfilesEntity> caseRoleEntities =
            repository.findRoleToAccessProfilesEntityByCaseType(caseType);

        return caseRoleEntities
            .stream()
            .map(dtoMapper::roleToAccessProfilesEntityToRoleAssignment)
            .collect(toList());
    }

}
