package uk.gov.hmcts.ccd.definition.store.repository.amint;

import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.hmcts.ccd.definition.store.repository.am.AMCaseTypeACLRepository;
import uk.gov.hmcts.ccd.definition.store.repository.am.CaseTypeAmInfo;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import javax.sql.DataSource;
import java.util.List;

public class AMCaseTypeAclDAO implements AMCaseTypeACLRepository {

    private static final String CASE_CONSTANT = "case";
    private DefaultRoleSetupImportService defaultRoleSetupImportService;

    public AMCaseTypeAclDAO(@Qualifier("amDataSource") DataSource dataSource) {
        defaultRoleSetupImportService = new DefaultRoleSetupImportService(dataSource);
    }

    @Override
    public CaseTypeAmInfo getAmInfoFor(String reference) {
        return null;
    }

    @Override
    public List<CaseTypeAmInfo> getAmInfoFor(List<String> caseTypeReference) {
        return null;
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
