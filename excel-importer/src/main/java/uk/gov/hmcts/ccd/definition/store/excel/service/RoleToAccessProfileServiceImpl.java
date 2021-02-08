package uk.gov.hmcts.ccd.definition.store.excel.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.ApplicationParams;
import uk.gov.hmcts.ccd.definition.store.domain.exception.NotFoundException;
import uk.gov.hmcts.ccd.definition.store.domain.service.CaseRoleServiceImpl;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.RoleToAccessProfileRepository;
import uk.gov.hmcts.ccd.definition.store.repository.accessprofile.GetCaseTypeRolesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.RoleToAccessProfileEntity;

@Component
public class RoleToAccessProfileServiceImpl implements RoleToAccessProfileService {

    private static final Logger LOG = LoggerFactory.getLogger(RoleToAccessProfileServiceImpl.class);

    private GetCaseTypeRolesRepository getCaseTypeRolesRepository;

    private RoleToAccessProfileRepository roleToAccessProfileRepository;

    private CaseTypeRepository caseTypeRepository;

    private ApplicationParams applicationParams;

    @Autowired
    public RoleToAccessProfileServiceImpl(GetCaseTypeRolesRepository getCaseTypeRolesRepository,
                                          CaseTypeRepository caseTypeRepository,
                                          RoleToAccessProfileRepository roleToAccessProfileRepository,
                                          ApplicationParams applicationParams) {
        this.getCaseTypeRolesRepository = getCaseTypeRolesRepository;
        this.roleToAccessProfileRepository = roleToAccessProfileRepository;
        this.caseTypeRepository = caseTypeRepository;
        this.applicationParams = applicationParams;
    }

    @Override
    public void createAccessProfileMapping(String caseTypeReference) {
        if (applicationParams.isRoleToAccessProfileMapping()) {
            LOG.info("Create Access Profile mapping for case type {}", caseTypeReference);
            CaseTypeEntity caseTypeEntity = caseTypeRepository.findCurrentVersionForReference(caseTypeReference)
                .orElseThrow(() -> new NotFoundException(caseTypeReference));
            Set<String> userAndCaseRoles = getCaseTypeRolesRepository.findCaseTypeRoles(caseTypeEntity.getId());
            roleToAccessProfileRepository.saveAll(createRoleToAccessProfilesEntity(userAndCaseRoles));
        }
    }

    private List<RoleToAccessProfileEntity> createRoleToAccessProfilesEntity(Set<String> userAndCaseRoles) {
        return userAndCaseRoles.
            stream()
            .map(role -> createRoleToAccessProfilesEntity(role))
            .collect(Collectors.toList());
    }

    private RoleToAccessProfileEntity createRoleToAccessProfilesEntity(String role) {
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
