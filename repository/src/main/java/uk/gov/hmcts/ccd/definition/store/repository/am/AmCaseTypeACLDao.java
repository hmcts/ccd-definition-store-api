package uk.gov.hmcts.ccd.definition.store.repository.am;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeACLEntity;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportServiceImpl;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;
import uk.gov.hmcts.reform.amlib.models.RolePermissionsForCaseTypeEnvelope;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.DELETE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.Permission.UPDATE;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;

@Component
public class AmCaseTypeACLDao implements AmCaseTypeACLRepository {

    private static final String CASE_CONSTANT = "case";
    private final AccessManagementService accessManagementService;
    private final DefaultRoleSetupImportServiceImpl defaultRoleSetupImportService;

    public AmCaseTypeACLDao(@Qualifier("amDataSource") DataSource dataSource) {
        accessManagementService = new AccessManagementService(dataSource);
        defaultRoleSetupImportService = new DefaultRoleSetupImportServiceImpl(dataSource);
    }

    @Override
    public CaseTypeAmInfo getAmInfoFor(String caseTypeReference) {

        RolePermissionsForCaseTypeEnvelope rolePermissionsForCaseTypeEnvelope =
            accessManagementService.returnRolePermissionsForCaseType(caseTypeReference);

        List<CaseTypeACLEntity> caseTypeACLEntities = new ArrayList<>();
        rolePermissionsForCaseTypeEnvelope.getDefaultRolePermissions().forEach(permissionsForRole -> {
            CaseTypeACLEntity caseTypeACLEntity = new CaseTypeACLEntity();
            caseTypeACLEntity.getCaseType().setName(rolePermissionsForCaseTypeEnvelope.getCaseTypeId());
            caseTypeACLEntity.getUserRole().setName(permissionsForRole.getRole());
            caseTypeACLEntity.setCreate(permissionsForRole.getPermissions().contains(CREATE));
            caseTypeACLEntity.setRead(permissionsForRole.getPermissions().contains(READ));
            caseTypeACLEntity.setUpdate(permissionsForRole.getPermissions().contains(UPDATE));
            caseTypeACLEntity.setDelete(permissionsForRole.getPermissions().contains(DELETE));
            caseTypeACLEntities.add(caseTypeACLEntity);
        });

        return CaseTypeAmInfo.builder()
            .caseReference(caseTypeReference)
            .caseTypeACLs(caseTypeACLEntities)
            .build();
    }

    @Override
    public List<CaseTypeAmInfo> getAmInfoFor(List<String> caseTypeReferences) {
        List<CaseTypeAmInfo> caseTypeAmInfos = new ArrayList<>();
        caseTypeReferences.forEach(caseTypeReference -> caseTypeAmInfos.add(getAmInfoFor(caseTypeReference)));
        return caseTypeAmInfos;
    }

    @Override
    public CaseTypeAmInfo saveAmInfoFor(CaseTypeAmInfo caseTypeAmInfo) {

        Map<String, List<DefaultPermissionGrant>> caseTypeRolePermissionsToSaveToAm = ImmutableMap.of(
            caseTypeAmInfo.getCaseReference(), createDefaultPermissionGrantsForCaseType(caseTypeAmInfo));

        defaultRoleSetupImportService.grantResourceDefaultPermissions(caseTypeRolePermissionsToSaveToAm);

        return caseTypeAmInfo;
    }

    @Override
    public List<CaseTypeAmInfo> saveAmInfoFor(List<CaseTypeAmInfo> caseTypeAmInfos) {
        setupAMServices(caseTypeAmInfos);
        Map<String, List<DefaultPermissionGrant>> caseTypeRolePermissionsToSaveToAm = new HashMap<>();
        caseTypeAmInfos.forEach(caseTypeAmInfo -> caseTypeRolePermissionsToSaveToAm.put(
            caseTypeAmInfo.getCaseReference(), createDefaultPermissionGrantsForCaseType(caseTypeAmInfo)));

        defaultRoleSetupImportService.grantResourceDefaultPermissions(caseTypeRolePermissionsToSaveToAm);

        return caseTypeAmInfos;
    }

    private List<DefaultPermissionGrant> createDefaultPermissionGrantsForCaseType(CaseTypeAmInfo caseTypeAmInfo) {
        List<DefaultPermissionGrant> rolePermissionsForCaseType = new ArrayList<>();

        ResourceDefinition resourceDefinition = createResourceDefinition(caseTypeAmInfo);

        caseTypeAmInfo.getCaseTypeACLs().forEach(caseTypeACLEntity -> {

            Set<Permission> permissions = new HashSet<>();
            if (caseTypeACLEntity.getCreate()) permissions.add(CREATE);
            if (caseTypeACLEntity.getRead()) permissions.add(READ);
            if (caseTypeACLEntity.getUpdate()) permissions.add(UPDATE);
            if (caseTypeACLEntity.getDelete()) permissions.add(DELETE);

            SecurityClassification securityClassification = SecurityClassification.valueOf(
                caseTypeACLEntity.getCaseType().getSecurityClassification().toString());

            DefaultPermissionGrant defaultPermissionGrant = DefaultPermissionGrant.builder()
                .resourceDefinition(resourceDefinition)
                .roleName(caseTypeACLEntity.getUserRole().getName())
                .attributePermissions(ImmutableMap.of(JsonPointer.valueOf(""),
                    new AbstractMap.SimpleEntry<>(permissions, securityClassification)))
                .lastUpdate(Instant.now())
                .changedBy("CCD Definition Store")
                .build();
            rolePermissionsForCaseType.add(defaultPermissionGrant);
        });

        return rolePermissionsForCaseType;
    }

    private ResourceDefinition createResourceDefinition(CaseTypeAmInfo caseTypeAmInfo) {
        return ResourceDefinition.builder()
            .serviceName(caseTypeAmInfo.getJurisdictionId())
            .resourceName(caseTypeAmInfo.getCaseReference())
            .resourceType(CASE_CONSTANT)
            .build();
    }

    private void setupAMServices(List<CaseTypeAmInfo> caseTypeAmInfos) {
        for (CaseTypeAmInfo caseTypeAmInfo : caseTypeAmInfos) {
            defaultRoleSetupImportService.addService(caseTypeAmInfo.getJurisdictionId());
            defaultRoleSetupImportService.addResourceDefinition(createResourceDefinition(caseTypeAmInfo));
            caseTypeAmInfo.getCaseTypeACLs().forEach(
                s -> defaultRoleSetupImportService.addRole(s.getUserRole().getName(), RoleType.IDAM, SecurityClassification.valueOf(
                    s.getCaseType().getSecurityClassification().toString()), AccessType.ROLE_BASED)
            );
        }
    }
}
