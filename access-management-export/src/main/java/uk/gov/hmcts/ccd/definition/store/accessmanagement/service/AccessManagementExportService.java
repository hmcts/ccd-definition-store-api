package uk.gov.hmcts.ccd.definition.store.accessmanagement.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;

import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

import java.util.List;

@Slf4j
@Component
public class AccessManagementExportService {

    @Autowired
    public AccessManagementExportService(DefaultRoleSetupImportService defaultRoleSetupImportService) {
        this.defaultRoleSetupImportService = defaultRoleSetupImportService;
    }

    private DefaultRoleSetupImportService defaultRoleSetupImportService;

    public void exportToAccessManagement(DefinitionImportedEvent event) {
        event.getContent().iterator().forEachRemaining(caseTypeEntity -> addService(caseTypeEntity));
    }

    void addService(CaseTypeEntity caseTypeEntity) {
        defaultRoleSetupImportService.addService(caseTypeEntity.getJurisdiction().getReference(), caseTypeEntity.getJurisdiction().getDescription());
        addRoles(caseTypeEntity.getCaseRoles());
        defaultRoleSetupImportService.addResourceDefinition(caseTypeEntity.getJurisdiction().getReference(), "CASE", caseTypeEntity.getReference());
    }

    private void addRoles(List<CaseRoleEntity> caseRoles) {
        caseRoles.iterator().forEachRemaining(caseRoleEntity ->
            defaultRoleSetupImportService.addRole(caseRoleEntity.getName(), RoleType.RESOURCE,
                SecurityClassification.valueOf(caseRoleEntity.getSecurityClassification().name()), AccessType.ROLE_BASED));
    }
}
