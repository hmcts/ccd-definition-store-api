package uk.gov.hmcts.ccd.definition.store.accessmanagement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.definition.store.accessmanagement.service.AccessManagementExportService;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
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
        listener.onDefinitionImported(newEvent(caseTypeEntity));
        verify(accessManagementExportService, times(1)).exportToAccessManagement(isA(DefinitionImportedEvent.class));
    }

    private DefinitionImportedEvent newEvent(CaseTypeEntity... caseTypes) {
        return new DefinitionImportedEvent(newArrayList(caseTypes));
    }
}
