package uk.gov.hmcts.ccd.definition.store.repository.amint;

import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.hmcts.ccd.definition.store.repository.AMCaseTypeACLRepository;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeAmInfo;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import javax.sql.DataSource;
import java.util.List;

public class AMCaseTypeAclDAO implements AMCaseTypeACLRepository {
    private DefaultRoleSetupImportService defaultRoleSetupImportService;

    public AMCaseTypeAclDAO(@Qualifier("amDataSource") DataSource dataSource) {
        defaultRoleSetupImportService = new DefaultRoleSetupImportService(dataSource);
    }

    @Override
    public CaseTypeAmInfo getAmInfoFor(String reference) {
      //  defaultRoleSetupImportService.
        return null;
    }

    @Override
    public List<CaseTypeAmInfo> getAmInfoFor(List<String> caseTypeReference) {
        return null;
    }

    @Override
    public CaseTypeAmInfo saveAmInfoFor(CaseTypeAmInfo caseTypeAmInfo) {
        ResourceDefinition resourceDefinition =
            new ResourceDefinition
                ("Will need service Name/Jurisdiction ID", "Hardcoded case", caseTypeAmInfo.getCaseReference());

        roleName/CaseRole??;

        /*    @NotNull

    private final ResourceDefinition resourceDefinition;   Checked

    private final String roleName;

    private final Map<@NotNull JsonPointer,  Entry< Set< Permission>, SecurityClassification>> attributePermissions;

    @JsonIgnore
    private Instant lastUpdate;

    @JsonIgnore
    private String callingServiceName;

    @JsonIgnore
    private String changedBy;

    @JsonIgnore
    private AuditAction action;*/

        defaultRoleSetupImportService.grantDefaultPermission();
        DefaultPermissionGrant.builder()
            .resourceDefinition(resourceDefinition)
            .attributePermissions(null)
            .build();
    }

    @Override
    public List<CaseTypeAmInfo> saveAmInfoFor(List<CaseTypeAmInfo> caseTypeAmInfos) {
        return null;
    }
}
