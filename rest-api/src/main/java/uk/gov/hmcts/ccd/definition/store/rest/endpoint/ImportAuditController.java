package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import com.microsoft.azure.storage.StorageException;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.definition.store.rest.model.ImportAudit;
import uk.gov.hmcts.ccd.definition.store.rest.service.AzureBlobStorageClient;

import java.util.Collection;

@RestController
@Api(value = "/api/import-audits")
@RequestMapping(value = "/api/import-audits")
class ImportAuditController {

    private final AzureBlobStorageClient azureBlobStorageClient;

    @Autowired
    ImportAuditController(final AzureBlobStorageClient azureBlobStorageClient) {
        this.azureBlobStorageClient = azureBlobStorageClient;
    }

    Collection<ImportAudit> fetchAllAudits() throws StorageException {
        return azureBlobStorageClient.fetchAllAudits();
    }
}
