package uk.gov.hmcts.ccd.definition.store.accessmanagement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;

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

        verify(defaultRoleSetupImportService).addResourceDefinition(caseTypeEntityA.getJurisdiction().getReference(), "CASE", caseTypeEntityA.getReference());
        verify(defaultRoleSetupImportService).addResourceDefinition(caseTypeEntityB.getJurisdiction().getReference(), "CASE", caseTypeEntityB.getReference());
    }

    private DefinitionImportedEvent newEvent() {
        caseTypeEntityA = new CaseTypeBuilder().withJurisdiction("jurA").withReference("caseTypeA").build();
        caseTypeEntityB = new CaseTypeBuilder().withJurisdiction("jurB").withReference("caseTypeB").build();
        return new DefinitionImportedEvent(newArrayList(caseTypeEntityA, caseTypeEntityB));
    }
}
