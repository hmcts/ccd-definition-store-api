package uk.gov.hmcts.ccd.definition.store.excel.service;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.ApplicationParams;
import uk.gov.hmcts.ccd.definition.store.domain.service.CaseRoleServiceImpl;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.RoleToAccessProfileRepository;
import uk.gov.hmcts.ccd.definition.store.repository.accessprofile.GetCaseTypeRolesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.RoleToAccessProfileEntity;

@Component
public class RoleToAccessProfileMappingServiceImpl implements RoleToAccessProfileMappingService {

    private static final Logger LOG = LoggerFactory.getLogger(RoleToAccessProfileMappingServiceImpl.class);

    private static final String MAPPING_NOT_REQUIRED = "Empty list of case type ids received, mapping is not required";

    private static final String MAPPING_COMPLETED = "Role to access profile mapping completed successfully";

    private GetCaseTypeRolesRepository getCaseTypeRolesRepository;

    private RoleToAccessProfileRepository roleToAccessProfileRepository;

    private CaseTypeRepository caseTypeRepository;

    private ApplicationParams applicationParams;

    @Autowired
    public RoleToAccessProfileMappingServiceImpl(GetCaseTypeRolesRepository getCaseTypeRolesRepository,
                                                 CaseTypeRepository caseTypeRepository,
                                                 RoleToAccessProfileRepository roleToAccessProfileRepository,
                                                 ApplicationParams applicationParams) {
        this.getCaseTypeRolesRepository = getCaseTypeRolesRepository;
        this.roleToAccessProfileRepository = roleToAccessProfileRepository;
        this.caseTypeRepository = caseTypeRepository;
        this.applicationParams = applicationParams;
    }

    @Override
    public String createAccessProfileMapping(Set<String> caseTypeIds) {
        if (caseTypeIds != null && caseTypeIds.size() > 0) {
            if (applicationParams.isRoleToAccessProfileMapping()) {
                caseTypeIds.stream().forEach(caseTypeReference -> {
                    LOG.info("Create Access Profile mapping for case type {}", caseTypeReference);
                    Optional<CaseTypeEntity> caseTypeEntity = caseTypeRepository
                        .findCurrentVersionForReference(caseTypeReference);

                    if (caseTypeEntity.isPresent()) {
                        Set<String> caseTypeRoles = getCaseTypeRolesRepository
                            .findCaseTypeRoles(caseTypeEntity.get().getId());
                        caseTypeRoles = filterMappingCompletedRoles(caseTypeReference, caseTypeRoles);

                        roleToAccessProfileRepository.saveAll(createRoleToAccessProfileEntities(caseTypeRoles));
                    }
                });
                return MAPPING_COMPLETED;
            }
        }
        return MAPPING_NOT_REQUIRED;
    }

    private Set<String>  filterMappingCompletedRoles(String caseTypeReference, Set<String> caseTypeRoles) {
        List<RoleToAccessProfileEntity> caseTypeMappings = roleToAccessProfileRepository
            .findByCaseTypeReference(Lists.newArrayList(caseTypeReference));

        Set<String> caseTypeRoleNames = caseTypeMappings.stream()
            .map(entity -> entity.getRoleName())
            .collect(Collectors.toSet());

       return caseTypeRoles.stream()
            .filter(role -> !caseTypeRoleNames.contains(role))
            .collect(Collectors.toSet());
    }

    private List<RoleToAccessProfileEntity> createRoleToAccessProfileEntities(Set<String> userAndCaseRoles) {
        return userAndCaseRoles.
            stream()
            .map(role -> createRoleToAccessProfileEntity(role))
            .collect(Collectors.toList());
    }

    private RoleToAccessProfileEntity createRoleToAccessProfileEntity(String role) {
        boolean isCaseRole = CaseRoleServiceImpl.isCaseRole(role);
        String roleName = isCaseRole ? role : "idam:" + role;
        RoleToAccessProfileEntity entity = new RoleToAccessProfileEntity();
        entity.setRoleName(roleName);
        entity.setAccessProfiles(role);
        entity.setReadOnly(false);
        entity.setDisabled(false);
        return entity;
    }
}
