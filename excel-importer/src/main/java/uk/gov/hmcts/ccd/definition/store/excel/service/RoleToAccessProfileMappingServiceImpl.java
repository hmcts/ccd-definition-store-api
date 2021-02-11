package uk.gov.hmcts.ccd.definition.store.excel.service;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
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

    private static final Pattern RESTRICT_GRANTED_ROLES_PATTERN
        = Pattern.compile(".+-solicitor$|.+-panelmember$|^citizen(-.*)?$|^letter-holder$|^caseworker-."
        + "+-localAuthority$");

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
        if (applicationParams.isRoleToAccessProfileMapping()) {
            if (caseTypeIds == null || caseTypeIds.size() == 0) {
                caseTypeIds = getAllCaseTypes();
            }
            caseTypeIds.stream().forEach(caseTypeReference -> {
                LOG.info("Create Access Profile mapping for case type {}", caseTypeReference);
                Optional<CaseTypeEntity> caseTypeEntity = caseTypeRepository
                    .findCurrentVersionForReference(caseTypeReference);

                if (caseTypeEntity.isPresent()) {
                    Set<String> caseTypeRoles = getCaseTypeRolesRepository
                        .findCaseTypeRoles(caseTypeEntity.get().getId());

                    deleteExistingMappings(caseTypeReference);

                    roleToAccessProfileRepository
                        .saveAll(createRoleToAccessProfileEntities(caseTypeRoles, caseTypeEntity.get()));
                }
            });
        }
        return MAPPING_COMPLETED;
    }

    private Set<String> getAllCaseTypes() {
        LOG.info("Get all existing case types");
        List<CaseTypeEntity> caseTypeEntities = caseTypeRepository.findAllLatestVersions();
        return caseTypeEntities.stream()
            .map(entity -> entity.getReference())
            .collect(Collectors.toSet());
    }

    private void deleteExistingMappings(String caseTypeReference) {
        List<RoleToAccessProfileEntity> caseTypeMappings = roleToAccessProfileRepository
            .findByCaseTypeReference(Lists.newArrayList(caseTypeReference));

        List<RoleToAccessProfileEntity> idamRolesAndCaseRoles = caseTypeMappings.stream()
            .filter(entity -> {
                String roleName = entity.getRoleName();
                return roleName.startsWith("idam:") || CaseRoleServiceImpl.isCaseRole(roleName);
            })
            .collect(Collectors.toList());

        roleToAccessProfileRepository.deleteAll(idamRolesAndCaseRoles);
        roleToAccessProfileRepository.flush();
    }

    private List<RoleToAccessProfileEntity> createRoleToAccessProfileEntities(Set<String> userAndCaseRoles,
                                                                              CaseTypeEntity caseTypeEntity) {
        return userAndCaseRoles.stream()
            .map(role -> createRoleToAccessProfileEntity(caseTypeEntity, role))
            .collect(Collectors.toList());
    }

    private RoleToAccessProfileEntity createRoleToAccessProfileEntity(CaseTypeEntity caseTypeEntity, String role) {
        boolean isCaseRole = CaseRoleServiceImpl.isCaseRole(role);
        String roleName = isCaseRole ? role : "idam:" + role;
        RoleToAccessProfileEntity entity = new RoleToAccessProfileEntity();
        entity.setRoleName(roleName);
        entity.setCaseType(caseTypeEntity);
        entity.setAccessProfiles(role);
        entity.setReadOnly(false);
        entity.setDisabled(false);
        entity.setRequiresCaseRole(RESTRICT_GRANTED_ROLES_PATTERN.matcher(role).matches());
        return entity;
    }
}
