package uk.gov.hmcts.ccd.definition.store.accessmanagement;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.accessmanagement.service.AccessManagementExportService;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.event.RoleImportedEvent;

@Slf4j
@ConditionalOnExpression("'${accessmanagement.enabled}'=='true'")
@Service
public class AccessManagementDefinitionImportListener {

    private AccessManagementExportService accessManagementExportService;

    @Autowired
    public AccessManagementDefinitionImportListener(AccessManagementExportService accessManagementExportService) {
        this.accessManagementExportService = accessManagementExportService;
    }

    @EventListener
    public void onDefinitionImported(DefinitionImportedEvent event) {
        accessManagementExportService.exportToAccessManagement(event);
    }

    @EventListener
    public void onRoleImported(RoleImportedEvent event) {
        accessManagementExportService.exportRoleToAccessManagement(event);
    }
}
