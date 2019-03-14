package uk.gov.hmcts.ccd.definition.store.accessmanagement.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.event.RoleImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

@Slf4j
@Component
public class AccessManagementExportService {

    private DefaultRoleSetupImportService defaultRoleSetupImportService;

    @Autowired
    public AccessManagementExportService(DefaultRoleSetupImportService defaultRoleSetupImportService) {
        this.defaultRoleSetupImportService = defaultRoleSetupImportService;
    }

    public void exportToAccessManagement(DefinitionImportedEvent event) {
        event.getContent().iterator().forEachRemaining(this::addService);
    }

    private void addService(CaseTypeEntity caseTypeEntity) {
        defaultRoleSetupImportService.addService(caseTypeEntity.getJurisdiction().getReference(), caseTypeEntity.getJurisdiction().getDescription());
        defaultRoleSetupImportService.addResourceDefinition(caseTypeEntity.getJurisdiction().getReference(), "CASE", caseTypeEntity.getReference());
    }

    public void exportRoleToAccessManagement(RoleImportedEvent event) {
        addRole(event.getContent());
    }

    private void addRole(UserRoleEntity userRoleEntity) {
        defaultRoleSetupImportService.addRole(userRoleEntity.getName(), RoleType.RESOURCE,
            SecurityClassification.valueOf(userRoleEntity.getSecurityClassification().name()), AccessType.ROLE_BASED);
    }
}
