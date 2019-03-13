package uk.gov.hmcts.ccd.definition.store.accessmanagement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.definition.store.accessmanagement.service.AccessManagementExportService;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.event.RoleImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccessManagementDefinitionImportListenerTest {

    private CaseTypeEntity caseTypeEntity = new CaseTypeBuilder().withJurisdiction("jurA").withReference("caseTypeA").build();

    @InjectMocks
    private AccessManagementDefinitionImportListener listener;

    @Mock
    private AccessManagementExportService accessManagementExportService;

    @Test
    public void shouldCallAccessManagementExportService() {
        listener.onDefinitionImported(newDefinitionImportedEvent(caseTypeEntity));
        verify(accessManagementExportService, times(1)).exportToAccessManagement(isA(DefinitionImportedEvent.class));

        listener.onRoleImported(newRoleImportedEvent());
        verify(accessManagementExportService, times(1)).exportRoleToAccessManagement(isA(RoleImportedEvent.class));
    }

    private DefinitionImportedEvent newDefinitionImportedEvent(CaseTypeEntity... caseTypes) {
        return new DefinitionImportedEvent(newArrayList(caseTypes));
    }

    private RoleImportedEvent newRoleImportedEvent() {
        UserRoleEntity role = new UserRoleEntity();
        role.setName("name");
        role.setSecurityClassification(SecurityClassification.valueOf("PUBLIC"));
        return new RoleImportedEvent(role);
    }
}
