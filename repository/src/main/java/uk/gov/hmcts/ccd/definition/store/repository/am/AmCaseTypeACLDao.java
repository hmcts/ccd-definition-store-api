package uk.gov.hmcts.ccd.definition.store.repository.am;

import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeACLEntity;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;
import uk.gov.hmcts.reform.amlib.models.RolePermissionsForCaseTypeEnvelope;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.DELETE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.Permission.UPDATE;

public class AmCaseTypeACLDao implements AmCaseTypeACLRepository {

    private static final String CASE_CONSTANT = "case";
    private final AccessManagementService accessManagementService;
    private final DefaultRoleSetupImportService defaultRoleSetupImportService;

    public AmCaseTypeACLDao(@Qualifier("amDataSource") DataSource dataSource) {
        accessManagementService = new AccessManagementService(dataSource);
        defaultRoleSetupImportService = new DefaultRoleSetupImportService(dataSource);
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
        ResourceDefinition resourceDefinition =
            new ResourceDefinition(caseTypeAmInfo.getJurisdictionId(), CASE_CONSTANT, caseTypeAmInfo.getCaseReference());

        /*

    private final ResourceDefinition resourceDefinition;

    private final String roleName;

    private final Map< JsonPointer,  Entry<@NotEmpty Set< Permission>,  SecurityClassification>> attributePermissions;
    *
    private Instant lastUpdate;

    private String callingServiceName;

    private String changedBy;

    private AuditAction action;*/

        return null;
    }

    @Override
    public List<CaseTypeAmInfo> saveAmInfoFor(List<CaseTypeAmInfo> caseTypeAmInfos) {
        return null;
    }
}
