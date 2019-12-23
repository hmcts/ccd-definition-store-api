package uk.gov.hmcts.ccd.definition.store.rest.service;

import com.microsoft.azure.storage.ResultContinuation;
import com.microsoft.azure.storage.ResultSegment;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobListingDetails;
import com.microsoft.azure.storage.blob.BlobProperties;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.excel.azurestorage.AzureStorageConfiguration;
import uk.gov.hmcts.ccd.definition.store.rest.model.ImportAudit;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import static java.util.Collections.sort;

@Service
@ConditionalOnProperty(name = "azure.storage.definition-upload-enabled")
public class AzureImportAuditsClient {

    public static final String USER_ID = "UserID";
    public static final String CASE_TYPES = "CaseTypes";

    private final CloudBlobContainer cloudBlobContainer;
    private final AzureStorageConfiguration azureStorageConfiguration;

    @Autowired
    public AzureImportAuditsClient(CloudBlobContainer cloudBlobContainer, AzureStorageConfiguration azureStorageConfiguration) {
        this.cloudBlobContainer = cloudBlobContainer;
        this.azureStorageConfiguration = azureStorageConfiguration;
    }

    /**
     * Fetches All import audits.
     *
     * @return import audits in reverse chronological order
     * @throws StorageException Exception thrown when trying to connect to Azure Blob store
     */
    public List<ImportAudit> fetchAllImportAudits() throws StorageException {
        List<ImportAudit> audits = new ArrayList<>();
        ResultContinuation nextBlobsPage = null;
        ResultSegment<ListBlobItem> blobsPage = cloudBlobContainer.listBlobsSegmented(null,
                                                                                      false,
                                                                                      EnumSet.noneOf(BlobListingDetails.class),
                                                                                      azureStorageConfiguration.getAzureImportAuditsGetLimit(),
                                                                                      nextBlobsPage,
                                                                                      null,
                                                                                      null);

        for (ListBlobItem lbi : blobsPage.getResults()) {
            if (lbi instanceof CloudBlockBlob) {
                final CloudBlockBlob cbb = (CloudBlockBlob) lbi;
                cbb.downloadAttributes();
                final ImportAudit audit = new ImportAudit();
                final BlobProperties properties = cbb.getProperties();
                final HashMap<String, String> metadata = cbb.getMetadata();
                final Date createdTime = properties.getCreatedTime();
                audit.setDateImported(Instant.ofEpochMilli(createdTime.getTime())
                                             .atZone(ZoneId.systemDefault())
                                             .toLocalDate());
                audit.setWhoImported(metadata.get(USER_ID));
                audit.setCaseType(metadata.get(CASE_TYPES));
                audit.setFilename(cbb.getName());
                audit.setUri(cbb.getUri());
                audit.setOrder(createdTime);
                audits.add(audit);
            }
        }
        sort(audits, (o1, o2) -> o2.getOrder().compareTo(o1.getOrder()));
        return audits;
    }
}
