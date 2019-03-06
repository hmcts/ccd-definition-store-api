package uk.gov.hmcts.ccd.definition.store.accessmanagement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AccessManagementExportServiceTest {

    @InjectMocks
    private AccessManagementExportService service;

    @Mock
    private DefaultRoleSetupImportService defaultRoleSetupImportService;

    private CaseTypeEntity caseTypeEntityA;
    private CaseTypeEntity caseTypeEntityB;

    @Test
    public void shouldCallAccessManagementExportService() {
        service.exportToAccessManagement(newEvent());
        verify(defaultRoleSetupImportService).addService(caseTypeEntityA.getJurisdiction().getReference(), null);
        verify(defaultRoleSetupImportService).addService(caseTypeEntityB.getJurisdiction().getReference(), null);

        verify(defaultRoleSetupImportService).addRole(caseTypeEntityA.getCaseRoles().iterator().next().getName(), RoleType.RESOURCE,
            uk.gov.hmcts.reform.amlib.enums.SecurityClassification.valueOf(caseTypeEntityA.getCaseRoles().iterator().next().getSecurityClassification().name()), AccessType.ROLE_BASED);
        verify(defaultRoleSetupImportService).addRole(caseTypeEntityB.getCaseRoles().iterator().next().getName(), RoleType.RESOURCE,
            uk.gov.hmcts.reform.amlib.enums.SecurityClassification.valueOf(caseTypeEntityB.getCaseRoles().iterator().next().getSecurityClassification().name()), AccessType.ROLE_BASED);

        verify(defaultRoleSetupImportService).addResourceDefinition(caseTypeEntityA.getJurisdiction().getReference(), "CASE", caseTypeEntityA.getReference());
        verify(defaultRoleSetupImportService).addResourceDefinition(caseTypeEntityB.getJurisdiction().getReference(), "CASE", caseTypeEntityB.getReference());
    }

    private DefinitionImportedEvent newEvent() {
        caseTypeEntityA = new CaseTypeBuilder().withJurisdiction("jurA").withReference("caseTypeA").build();
        CaseRoleEntity caseRoleEntityA = new CaseRoleEntity();
        caseRoleEntityA.setSecurityClassification(SecurityClassification.PUBLIC);
        caseRoleEntityA.setName("caseRoleA");
        caseTypeEntityA.addCaseRole(caseRoleEntityA);

        caseTypeEntityB = new CaseTypeBuilder().withJurisdiction("jurB").withReference("caseTypeB").build();
        CaseRoleEntity caseRoleEntityB = new CaseRoleEntity();
        caseRoleEntityB.setName("caseRoleB");
        caseRoleEntityB.setSecurityClassification(SecurityClassification.PUBLIC);
        caseTypeEntityB.addCaseRole(caseRoleEntityB);

        return new DefinitionImportedEvent(newArrayList(caseTypeEntityA, caseTypeEntityB));
    }
}
