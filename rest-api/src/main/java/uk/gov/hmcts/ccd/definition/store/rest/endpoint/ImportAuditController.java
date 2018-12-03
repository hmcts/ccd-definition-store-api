package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import com.microsoft.azure.storage.StorageException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.definition.store.rest.model.ImportAudit;
import uk.gov.hmcts.ccd.definition.store.rest.service.AzureBlobStorageClient;

import java.util.Collection;

import static java.util.Collections.emptyList;

@RestController
@Api(value = "/api/import-audits")
@RequestMapping(value = "/api")
class ImportAuditController {

    private final AzureBlobStorageClient azureBlobStorageClient;

    @Autowired
    ImportAuditController(@Autowired(required = false) final AzureBlobStorageClient azureBlobStorageClient) {
        this.azureBlobStorageClient = azureBlobStorageClient;
    }

    @RequestMapping(value = "/import-audits", method = RequestMethod.GET, produces = {"application/json"})
    @ApiOperation(value = "Fetches import audits")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Import audits")
    })
    Collection<ImportAudit> fetchAllAudits() throws StorageException {
        if (null != azureBlobStorageClient) {
            return azureBlobStorageClient.fetchAllAudits();
        } else {
            return emptyList();
        }
    }
}
